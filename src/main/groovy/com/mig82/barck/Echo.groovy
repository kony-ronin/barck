class Echo{
	private static Boolean noisy = false
	public static void setNoisy(){
		this.noisy = true
	}
	public static void say(String s){
		if(noisy){println s}
	}
}