package my.db.classes;

import my.db.classes.pkg.food.Fruit;

public class Person {
	public String name;
	public int age;
	public Fruit favoriteFruit;

	public Fruit getFavoriteFruit() {
		return this.favoriteFruit;
	}
}
