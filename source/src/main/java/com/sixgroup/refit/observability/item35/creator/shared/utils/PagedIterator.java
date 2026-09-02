package com.sixgroup.refit.observability.item35.creator.shared.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public final class PagedIterator<T> implements Iterator<T> {

    private final Function<Pageable, Slice<T>> pageLoader;
    private final int pageSize;
    private Iterator<T> currentPage = Collections.emptyIterator();
    private int pageNumber;
    private boolean finished;

    public PagedIterator(Function<Pageable, Slice<T>> pageLoader, int pageSize) {
        this.pageLoader = pageLoader;
        this.pageSize = pageSize;
    }

    @Override
    public boolean hasNext() {
        while (!currentPage.hasNext() && !finished) {
            Slice<T> page = pageLoader.apply(PageRequest.of(pageNumber++, pageSize));
            currentPage = page.iterator();
            finished = !page.hasNext();
        }
        return currentPage.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return currentPage.next();
    }
}
