package com.sixgroup.refit.observability.item35.creator.application.mock;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants;

import java.util.List;

public class CapacityMock {

    public static List<Capacity> builderListCapacityRam() {

        return List.of(
            new Capacity("2024-01-01", "3.0897053696E10", "3.0522986496E10", "3.0679712110933334E10", CapacityConstants.RAM),
            new Capacity("2024-01-01", "4.7310544896E10", "4.5103108096E10", "4.569065137777778E10", CapacityConstants.RAM),
            new Capacity("2024-01-01", "4.1640742912E10", "3.5949477888E10", "3.762765280426667E10", CapacityConstants.RAM),
            new Capacity("2024-01-01", "9.4086873088E10", "9.1223027712E10", "9.224051669333333E10", CapacityConstants.RAM),
            new Capacity("2024-01-01", "4.0806617088E10", "3.9146037248E10", "3.9659996570596245E10", CapacityConstants.RAM));
    }

    public static List<Capacity> builderListTotalCapacityRam() {

        return List.of(
            new Capacity("2024-01-01", "1.34283710464E11", "1.34283710464E11", "1.34283710464E11", CapacityConstants.RAM),
            new Capacity("2024-01-01", "1.3428371456E11", "1.3428371456E11", "1.3428371456E11", CapacityConstants.RAM),
            new Capacity("2024-01-01", "1.34283710464E11", "1.34283710464E11", "1.34283710464E11", CapacityConstants.RAM),
            new Capacity("2024-01-01", "1.34283710464E11", "1.34283710464E11", "1.34283710464E11", CapacityConstants.RAM),
            new Capacity("2024-01-01", "1.34283710464E11", "1.34283710464E11", "1.34283710464E11", CapacityConstants.RAM));
    }

    public static List<Capacity> builderListCapacityCpu() {

        return List.of(
            new Capacity("2024-01-01", "3.0897053696E10", "3.0522986496E10", "3.0679712110933334E10", CapacityConstants.CPU),
            new Capacity("2024-01-01", "4.7310544896E10", "4.5103108096E10", "4.569065137777778E10", CapacityConstants.CPU),
            new Capacity("2024-01-01", "4.1640742912E10", "3.5949477888E10", "3.762765280426667E10", CapacityConstants.CPU),
            new Capacity("2024-01-01", "9.4086873088E10", "9.1223027712E10", "9.224051669333333E10", CapacityConstants.CPU),
            new Capacity("2024-01-01", "4.0806617088E10", "3.9146037248E10", "3.9659996570596245E10", CapacityConstants.CPU));
    }
}
