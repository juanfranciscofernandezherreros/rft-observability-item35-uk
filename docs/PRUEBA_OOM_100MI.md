# Prueba OOM del microservicio Item 35 con 100 MiB

> **Estado actual:** la generacion sintetica de 800.000 filas usada al preparar esta prueba fue eliminada el 2 de septiembre de 2026. El limite de 100 MiB permanece documentado, pero la version actual no repite registros.

## 1. Objetivo

El objetivo de esta prueba es comprobar el comportamiento de Kubernetes cuando el microservicio `rft-observability-item35-creator` dispone unicamente de `100Mi` de memoria.

Se busca verificar que:

- Kubernetes aplica el limite configurado;
- el contenedor es terminado al superar ese limite;
- la terminacion aparece como `OOMKilled`;
- el proceso devuelve el codigo de salida `137`; y
- el pod es reiniciado automaticamente y entra en `CrashLoopBackOff` si vuelve a fallar.

## 2. Entorno de la prueba

La prueba utiliza:

- un cluster local Kind llamado `item35-oom`;
- Kubernetes 1.36.1;
- la imagen local `rft-observability-item35-creator:compose`;
- Kafka y Schema Registry ejecutandose mediante Docker Compose; y
- el namespace Kubernetes `rft-observability-item35`.

La instancia Compose del microservicio `item35` se detuvo durante la prueba para evitar que consumiera los mismos mensajes Kafka que el pod.

## 3. Configuracion de memoria

El overlay utilizado esta en `k8s-oom-100mi/kustomization.yaml` y aplica los siguientes recursos al contenedor:

```yaml
resources:
  requests:
    memory: 100Mi
  limits:
    memory: 100Mi
```

`100Mi` equivale a 104.857.600 bytes. Kubernetes utiliza el limite como restriccion efectiva del cgroup del contenedor.

El mismo overlay configura `component-config.csv.row-count` con 800.000 para que, si el servicio completa el arranque, cada solicitud intente generar un CSV con 800.000 registros.

### 3.1 Como se generan los 800.000 registros

La cantidad de filas se controla mediante esta propiedad:

```yaml
component-config:
  csv:
    row-count: 800000
```

En el entorno Docker Compose la propiedad equivalente se encuentra en `application-compose.yml` como `component-config.csv.row_count: 800000`. En el overlay Kubernetes de esta prueba se establece mediante `SPRING_APPLICATION_JSON`.

Los cuatro escritores de CSV utilizan el helper `CsvRows.write`. El comportamiento esencial es:

```java
int rowCount = configuredRowCount > 0
    ? configuredRowCount
    : source.size();

for (int index = 0; index < rowCount; index++) {
    writer.accept(source.get(index % source.size()));
}
```

El proceso es el siguiente:

1. Cada caso de uso obtiene primero su lista de datos desde la fuente correspondiente.
2. El escritor crea el CSV y escribe su cabecera.
3. Si `row-count` es mayor que cero, el bucle se ejecuta exactamente 800.000 veces.
4. `index % source.size()` selecciona circularmente un elemento de la lista fuente.
5. Cada elemento seleccionado se escribe inmediatamente en el `CSVWriter`.

Por ejemplo, si la fuente local devuelve tres registros:

```text
A, B, C
```

la salida se genera asi:

```text
A, B, C, A, B, C, A, B, C, ...
```

hasta alcanzar exactamente 800.000 filas de datos. El fichero contiene ademas una cabecera, por lo que tiene 800.001 lineas fisicas.

Esta implementacion no construye otra lista con 800.000 objetos. Conserva la lista original y escribe los registros uno a uno. Esto reduce el consumo de memoria durante la prueba, pero implica que las filas locales se repiten y no representan 800.000 registros de negocio unicos.

Las fuentes originales son distintas para cada reporte:

| Reporte | Fuente real | Fuente usada localmente |
| --- | --- | --- |
| ITEM35A, Submission Volumes | tabla Kudu `record_status`, con resultados agrupados | H2 inicializado con `init_kudu.sql` |
| ITEM35B, Report Generation | `reports_file_outgoing`, `regu_identity` y `report_eod_state` | H2 inicializado con los scripts locales |
| ITEM35C, Storage Capacity | API de Cloudera | `total_all_MBT.json` y `free_all_MBT.json` |
| ITEM35D, Compute Capacity | API de Cloudera | JSON locales de CPU, RAM usada y RAM total |

Si la lista fuente esta vacia, el helper no inventa registros y el CSV no recibe filas de datos. Si `row-count` vale cero o no se configura, se escribe unicamente el numero real de elementos devueltos por la fuente.

En la prueba especifica de `100Mi`, este mecanismo no llega a ejecutarse. El pod es terminado por OOM durante la inicializacion de Spring y JPA, antes de consumir los eventos Kafka y antes de abrir los CSV. Por tanto, la configuracion de 800.000 filas esta preparada, pero el fallo observado ocurre antes de la generacion.

## 4. Preparacion

El cluster se creo con:

```powershell
kind create cluster --config kind-config.yaml
```

La imagen local se cargo dentro del nodo Kind:

```powershell
kind load docker-image rft-observability-item35-creator:compose `
  --name item35-oom
```

El despliegue de prueba se aplico con:

```powershell
kubectl apply -k k8s-oom-100mi
```

La configuracion efectiva se comprobo con:

```powershell
kubectl get deployment rft-observability-item35-creator `
  -n rft-observability-item35 `
  -o jsonpath="request={.spec.template.spec.containers[0].resources.requests.memory}{' limit='}{.spec.template.spec.containers[0].resources.limits.memory}"
```

Resultado:

```text
request=100Mi limit=100Mi
```

## 5. Lanzamiento de los cuatro reportes

Se enviaron consecutivamente al topic `rft.dev.observability.item.private.v1` cuatro eventos Avro con estos tipos:

- `submissionVolumes`;
- `reportGeneration`;
- `storageCapacity`;
- `computeCapacity`.

Los eventos quedaron publicados en Kafka. El offset final observado para la particion fue:

```text
rft.dev.observability.item.private.v1:0:14
```

## 6. Primera ejecucion

El primer pod desplegado fue:

```text
rft-observability-item35-creator-6dff79876-czv6x
```

Estado observado despues de publicar los eventos:

```text
ready=false
restarts=4
state=CrashLoopBackOff
lastReason=OOMKilled
exitCode=137
```

Kubernetes creo y arranco repetidamente el contenedor. Cada ejecucion supero el limite de `100Mi`, fue terminada por el kernel y Kubernetes aplico una espera progresiva antes del siguiente reinicio.

## 7. Segunda ejecucion independiente

Para repetir la prueba se forzo un nuevo rollout:

```powershell
kubectl rollout restart `
  deployment/rft-observability-item35-creator `
  -n rft-observability-item35
```

Esto creo un pod nuevo:

```text
rft-observability-item35-creator-76d9cbdb58-nkj54
```

Resultado de la segunda ejecucion:

```text
request=100Mi
limit=100Mi
restarts=3
reason=OOMKilled
exitCode=137
```

La segunda ejecucion reproduce el mismo resultado y descarta que el primer OOM fuera un fallo puntual.

## 8. Punto exacto del fallo

Los ultimos logs disponibles del contenedor terminado muestran:

```text
Starting ApplicationMain v1.11.0-SNAPSHOT using Java 17.0.20
The following 2 profiles are active: "k8s", "uk"
Bootstrapping Spring Data JPA repositories in DEFAULT mode.
Finished Spring Data repository scanning.
```

El proceso es terminado durante la inicializacion de Spring y JPA. No llega a completar el arranque, no alcanza el estado `Ready` y no inicia el consumo normal de los cuatro eventos Kafka.

Por tanto, esta prueba demuestra que `100Mi` es insuficiente incluso para arrancar el microservicio. No demuestra un OOM causado por la escritura de los cuatro CSV, porque la generacion de reportes no llega a comenzar.

## 9. Interpretacion de OOMKilled y codigo 137

`OOMKilled` indica que el contenedor supero el limite de memoria de su cgroup y fue terminado por el sistema.

El codigo `137` corresponde a:

```text
128 + 9 = 137
```

El valor `9` es la senal `SIGKILL`. El proceso no puede capturarla ni realizar un cierre ordenado.

`CrashLoopBackOff` no es la causa original. Es el estado que Kubernetes aplica cuando un contenedor falla y se reinicia repetidamente, aumentando progresivamente el tiempo entre intentos.

## 10. Comandos de observacion

Ver los reinicios en tiempo real:

```powershell
kubectl get pods -n rft-observability-item35 -w
```

Consultar la causa y el codigo de salida:

```powershell
kubectl get pod -n rft-observability-item35 `
  -l app.kubernetes.io/name=rft-observability-item35-creator `
  -o jsonpath="{range .items[*]}{.metadata.name}{' restarts='}{.status.containerStatuses[0].restartCount}{' reason='}{.status.containerStatuses[0].lastState.terminated.reason}{' exitCode='}{.status.containerStatuses[0].lastState.terminated.exitCode}{'\n'}{end}"
```

Consultar los logs de la ejecucion anterior del contenedor:

```powershell
kubectl logs -n rft-observability-item35 `
  -l app.kubernetes.io/name=rft-observability-item35-creator `
  --previous `
  --tail=100
```

Consultar los eventos Kubernetes:

```powershell
kubectl get events -n rft-observability-item35 `
  --sort-by=.lastTimestamp
```

## 11. Conclusion

La prueba fue reproducida dos veces y en ambas Kubernetes registro:

- limite aplicado: `100Mi`;
- pod no preparado: `Ready=false`;
- terminacion: `OOMKilled`;
- codigo de salida: `137`;
- reinicios automaticos; y
- estado final: `CrashLoopBackOff`.

El microservicio necesita mas de `100Mi` solo para completar su inicializacion. Para probar un OOM especificamente durante la generacion concurrente de reportes, primero debe determinarse un limite que permita arrancar y mantenerse estable en reposo, pero que sea inferior al pico de generacion. La prueba anterior de 800.000 filas midio aproximadamente `703,50 MiB` antes de los reportes y un pico de `1.066,46 MiB`, por lo que un limite intermedio debe validarse de forma incremental.
