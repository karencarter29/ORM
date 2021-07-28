package client.entities;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity("Zoo")
public class Zoo {

    @Id
    private Long id;

    @Column("NAME")
    private String name;

    @OneToMany
    private final List<Animal> zoo;

    public Zoo(String name) {
        this.name = name;
        zoo = new ArrayList<>();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Animal> getAnimals() {
        return zoo;
    }

    public void addAnimal(Animal animal) {
        zoo.add(animal);
        animal.setZoo(this);
    }
}
