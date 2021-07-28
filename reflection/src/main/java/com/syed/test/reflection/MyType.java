package com.syed.test.reflection;

public class MyType {
    private int id;
    private String name;
    private boolean manager;
    private double salaryPackage;

    public MyType(int id, String name, boolean manager, double salaryPackage) {
        this.id = id;
        this.name = name;
        this.manager = manager;
        this.salaryPackage = salaryPackage;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isManager() {
        return manager;
    }

    public double getSalaryPackage() {
        return salaryPackage;
    }
}
