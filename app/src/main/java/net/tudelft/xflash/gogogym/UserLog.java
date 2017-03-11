package net.tudelft.xflash.gogogym;

public class UserLog {

	public int id;
	public int user_id;
	public int gym_id;
	public String start_time;
	public String finish_time;


	public UserLog(int id, int user_id, int gym_id, String start_time, String finish_time) {
		this.id = id;
		this.user_id = user_id;
		this.gym_id = gym_id;
		this.start_time = start_time;
		this.finish_time = finish_time;
	}

	public String stringify_UData() {
		return id + ";" + user_id + ";" + gym_id + ";" + start_time + ";" + finish_time + ";";
	}
}