package orm.manager;

import org.reflections.Reflections;
import orm.annotations.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

public class OrmManager {

    private final Map<Class<?>, Map<Long, Object>> cache = new HashMap<>();
    private Set<Class<?>> set;
    private final String bdName;
    private OrmManager(String name) {
        bdName = name;
    }

    private void execute(String query) {
        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void prepareRepositoryFor(Class<?> c) {
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ").append(c.getSimpleName()).append("(");
        for (Field f : c.getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                query.append("ID INT NOT NULL PRIMARY KEY AUTO_INCREMENT, ");
            }
            if (f.isAnnotationPresent(Column.class)) {
                if ("name".equalsIgnoreCase(f.getName())) {
                    query.append("NAME VARCHAR (30) NOT NULL, ");
                }
                if ("age".equalsIgnoreCase(f.getName())) {
                    query.append("AGE INT NOT NULL, ");
                }
            }
            if(f.isAnnotationPresent(ManyToOne.class)) {
                Class<?> type = f.getType();
                if (set.contains(type)) {
                    prepareRepositoryFor(type);
                }
                for (Field field: type.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Id.class)) {
                        query.append(type.getSimpleName()).append("_").append(field.getName())
                                .append(" INT, FOREIGN KEY (").append(type.getSimpleName()).append("_")
                                .append(field.getName()).append(") REFERENCES ").append(type.getSimpleName())
                                .append("(").append(field.getName()).append("), ");
                    }
                }
            }
        }
        query.append(" );");
        execute(query.toString());
    }

    public void scanPackages(Class<?> c) {
        Reflections ref = new Reflections(c.getPackageName());
        set = ref.getTypesAnnotatedWith(Entity.class);
        for (var cl : set) {
            prepareRepositoryFor(cl);
        }
    }

    public Connection getConnection() {
        Connection con = null;
        try (InputStream input = new FileInputStream("src/main/resources/property.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            prop.getProperty(bdName + ".url");
            Class.forName("org.h2.Driver");
            con = DriverManager.getConnection(prop.getProperty(bdName + ".url"),
                    prop.getProperty(bdName + ".user"), prop.getProperty(bdName + ".password"));
        } catch (IOException | ClassNotFoundException | SQLException exc) {
            exc.printStackTrace();
        }
        return con;
    }

    public static OrmManager get(String name) {
        return new OrmManager(name);
    }

    public void save(Object object) throws IllegalAccessException {
        StringBuilder query = new StringBuilder();
        Class<?> cl = object.getClass();
        query.append("INSERT INTO ");
        if (!cl.isAnnotationPresent(Entity.class)) {
            throw new IllegalStateException("The class of the object isn't an Entity");
        }
        query.append(cl.getAnnotation(Entity.class).value()).append(" (");
        StringBuilder values = new StringBuilder();
        values.append(" VALUES (");
        boolean oneToMany = false;
        Field fOneToMany = null;
        boolean first = true;
        Field fieldId = null;
        for (Field field : cl.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                if (field.get(object) != null) {
                    throw new IllegalArgumentException("The element has already exist. Use update() instead save()");
                }
                fieldId = field;
            }
            if (field.isAnnotationPresent(Column.class)) {
                if (!first) {
                    query.append(", ");
                    values.append(", ");
                }
                first = false;
                query.append(field.getAnnotation(Column.class).value());
                if (field.getType().equals(String.class)) {
                    values.append("'").append(field.get(object)).append("'");
                } else {
                    values.append(field.get(object));
                }
            }
            if (field.isAnnotationPresent(OneToMany.class)) {
                oneToMany = true;
                fOneToMany = field;
            }
            if (field.isAnnotationPresent(ManyToOne.class)) {
                if (!first) {
                    query.append(", ");
                    values.append(", ");
                }
                first = false;
                if (field.get(object) != null) {
                    for (Field f : field.get(object).getClass().getDeclaredFields()) {
                        f.setAccessible(true);
                        if (f.isAnnotationPresent(Id.class)) {
                            if (f.get(field.get(object)) == null) {
                                save(field.get(object));
                            }
                        }
                    }
                }
                query.append(field.getAnnotation(ManyToOne.class).value());
                for (Field f : field.getType().getDeclaredFields()) {
                    f.setAccessible(true);
                    if (f.isAnnotationPresent(Id.class)) {
                        values.append(f.get(field.get(object)));
                    }
                }
            }
        }
        query.append(")").append(values).append(");");
        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate(query.toString(), Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            while (fieldId != null && rs.next()) {
                fieldId.set(object, rs.getLong(1));
            }
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
        if (oneToMany) {
            Object ob = fOneToMany.get(object);
            if (ob instanceof Collection) {
                for (Object el : (Collection<?>) ob) {
                    save(el);
                }
            }
        }
    }

    public void update(Object object) throws IllegalAccessException {
        StringBuilder query = new StringBuilder();
        Class<?> cl = object.getClass();
        query.append("UPDATE ");
        if (!cl.isAnnotationPresent(Entity.class)) {
            throw new IllegalStateException("The class of the object isn't an Entity");
        }
        query.append(cl.getAnnotation(Entity.class).value()).append(" SET ");
        Object id = null;
        boolean first = true;
        for (Field field : cl.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                id = field.get(object);
            }
            if (field.isAnnotationPresent(Column.class)) {
                if (!first) {
                    query.append(", ");
                }
                first = false;
                query.append(field.getAnnotation(Column.class).value())
                        .append("=");
                if (field.getType().equals(String.class)) {
                    query.append("'").append(field.get(object)).append("'");
                } else {
                    query.append(field.get(object));
                }
            }
            if (field.isAnnotationPresent(ManyToOne.class)) {
                if (!first) {
                    query.append(", ");
                }
                first = false;
                query.append(field.getAnnotation(ManyToOne.class).value()).append("=");
                for (Field f : field.getType().getDeclaredFields()) {
                    f.setAccessible(true);
                    if (f.isAnnotationPresent(Id.class)) {
                        query.append(f.get(field.get(object)));
                    }
                }
            }
        }
        query.append(" WHERE ID=").append(id).append(";");
        execute(query.toString());
    }

    public void delete(Object ob) throws IllegalAccessException {
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM ");
        query.append(ob.getClass().getAnnotation(Entity.class).value());
        query.append(" WHERE ID=");
        boolean flag = false;
        Field fManyToOne = null;
        for (Field f : ob.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(ManyToOne.class)) {
                flag = true;
                fManyToOne = f;
            }
            if (f.isAnnotationPresent(Id.class)) {
                f.setAccessible(true);
                try{
                    query.append(f.get(ob));
                } catch(IllegalAccessException exc) {
                    exc.printStackTrace();
                }
            }
        }
        query.append(" ;");
        execute(query.toString());
        if (flag) {
            fManyToOne.setAccessible(true);
            Object object = fManyToOne.get(ob);
            for (Field f : object.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(OneToMany.class)) {
                    Object obj = f.get(object);
                    if (obj instanceof Collection) {
                        ((Collection<?>) obj).removeIf(ob::equals);
                    }
                }
            }
        }
    }

    public List<?> getAllBy(Class<?> child, Class<?> parent) {
        for (Field field : parent.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(OneToMany.class)) {
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                for (Type t : pt.getActualTypeArguments()) {
                    if (t.equals(child)) {

                    }
                }
            }
        }
        return null;
    }

    public Object getById(Class<?> cl, Long id) {
        return null;
    }

    public List<?> getAll(Class<?> cl) {
        return null;
    }
}