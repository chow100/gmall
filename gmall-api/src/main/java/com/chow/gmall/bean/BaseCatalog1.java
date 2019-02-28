package com.chow.gmall.bean;

import javax.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 * @param
 * @return
 */
public class BaseCatalog1 implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String name;

    @Transient
    List<BaseCatalog2> baseCatalog2s;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BaseCatalog2> getBaseCatalog2s() {
        return baseCatalog2s;
    }

    public void setBaseCatalog2s(List<BaseCatalog2> baseCatalog2s) {
        this.baseCatalog2s = baseCatalog2s;
    }
}

