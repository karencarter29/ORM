package client.entities;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;

@Entity("Employee")
public class Employee {

    public Employee(String name) {
        this.name = name;
    }

    @Id
    Long id;

    @Column("NAME")
    String name;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
