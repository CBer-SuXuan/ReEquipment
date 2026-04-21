package org.reuac.reequipment.model;

import java.util.List;

public class LevelLores {
    private List<String> lores;

    // Constructor, getters, and setters

    public LevelLores(List<String> lores) {
        this.lores = lores;
    }

    public List<String> getLores() {
        return lores;
    }

    public void setLores(List<String> lores) {
        this.lores = lores;
    }
}