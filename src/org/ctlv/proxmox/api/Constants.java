package org.ctlv.proxmox.api;

public class Constants {
	
	public static String USER_NAME = "dwu";
	public static String PASS_WORD = "nGT!xPZw";
	
	public static String HOST = "srv-px1.insa-toulouse.fr";
	public static String REALM = "Ldap-INSA";
	
	public static String SERVER1 = "srv-px1";
	public static String SERVER2 = "srv-px2";
	public static String CT_BASE_NAME = "ct-tpgei-ctlv-bXXX-ct";
	public static long CT_BASE_ID = 21700;
	
	public static long GENERATION_WAIT_TIME = 10;
	public static String CT_TEMPLATE = "template:vztmpl/debian-8-turnkey-nodejs_14.2-1_amd64.tar.gz";
	public static String CT_PASSWORD = "tpuser";
	public static String CT_HDD = "vm:3";
	public static String CT_NETWORK = "name=eth0,bridge=vmbr1,ip=dhcp,tag=2028,type=veth";
	
	public static float CT_CREATION_RATIO_ON_SERVER1 = 0.66f;
	public static float CT_CREATION_RATIO_ON_SERVER2 = 0.33f;
	public static long RAM_SIZE[] = new long[]{256, 512, 768};
	
	public static long MONITOR_PERIOD = 30;
	public static float MIGRATION_THRESHOLD = 0.08f;
	public static float DROPPING_THRESHOLD = 0.12f;
	public static float MAX_THRESHOLD = 0.16f;
			

}
