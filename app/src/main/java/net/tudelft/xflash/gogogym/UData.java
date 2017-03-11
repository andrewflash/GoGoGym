package net.tudelft.xflash.gogogym;

public class UData {

	public int id;
	public String user_name;
	public String email;
	public String password;
	public int user_age;
	public String pet_name;
	public int pet_energy;
	public int pet_exp;

	public UData() {
	}

	public UData(int id, String user_name, String email, String password, int user_age, String pet_name, int pet_energy, int pet_exp) {
		this.id = id;
		this.user_name = user_name;
		this.email = email;
		this.password = password;
		this.user_age = user_age;
		this.pet_name = pet_name;
		this.pet_energy = pet_energy;
		this.pet_exp = pet_exp;
	}

	public String stringify_UData() {
		return id + ";" + user_name + ";" + email + ";" + password + ";" + user_age + ";" + pet_name + ";" + pet_energy + ";" + pet_exp + ";";
	}
}