package net.tudelft.xflash.gogogym;

public class UserLog {

	public int id;
	public int user_id;
	public int gym_id;
	public String start_time;
	public String finish_time;
	public String log_desc;

	public UserLog(int id, int user_id, int gym_id, String start_time, String finish_time, String log_desc) {
		this.id = id;
		this.user_id = user_id;
		this.gym_id = gym_id;
		this.start_time = start_time;
		this.finish_time = finish_time;
		this.log_desc = log_desc;
	}

	public String getImage() {
		if(this.log_desc.equals(Constants.ACTIVE_DESC)){
			return "ic_directions_run_black_24dp";
		}
		else if (this.log_desc.equals(Constants.VISIT_DESC)){
			return "ic_beenhere_black_24dp";
		}
		else return this.log_desc;
	}

	public String stringify_UserLog() {
		return id + ";" + user_id + ";" + gym_id + ";" + start_time + ";" + finish_time + ";" + log_desc + ";";
	}
}