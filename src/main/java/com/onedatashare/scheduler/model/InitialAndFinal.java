package com.onedatashare.scheduler.model;

import lombok.Data;

@Data
public class InitialAndFinal<T> {
    T start;
    T end;

    public InitialAndFinal(T start, T end) {
        this.start = start;
        this.end = end;
    }

}
