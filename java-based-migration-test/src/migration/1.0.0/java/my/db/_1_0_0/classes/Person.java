package my.db._1_0_0.classes;
import my.db._1_0_0.classes.pkg.food.Fruit;
public class Person {
    public String name;

    public int age;

    public Fruit favoriteFruit;

    public Fruit getFavoriteFruit() {
        return this.favoriteFruit;
    }
}