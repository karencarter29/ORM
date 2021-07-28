package orm.manager;

import client.entities.Animal;
import client.entities.Employee;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class OrmManagerTest {

    private OrmManager manager;

    @BeforeEach
    void setUp() {
        manager = OrmManager.get("DataBase");
        manager.scanPackages(Animal.class);
    }

    @AfterEach
    void tearDown() {
        manager = null;
    }


}