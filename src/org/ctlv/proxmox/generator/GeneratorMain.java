package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class GeneratorMain {

	private static final double THRESHOLD_EQUILLIBRE = 0.5;
	private static final double THRESHOLD_STOPCT = 0.75;
	static Random rndTime = new Random(new Date().getTime());
	public static int getNextEventPeriodic(int period) {
		return period;
	}
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}

	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {


		long baseID = Constants.CT_BASE_ID;
		int lambda = 30;


		Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();

		ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		Random rndRAM = new Random(new Date().getTime()); 

		long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
		long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);

		int num=0;
	
		ArrayList<String> listCTsrv1 = new ArrayList<>();
		ArrayList<String> listCTsrv2 = new ArrayList<>();
		
		
		int idCTcree=0;
		//test pour ct < 10 
		while (true) {
			//---------analyser les informations	
			// 1. Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
			long memOnServer1 = 0;
			// ...
			for (LXC lxc : api.getCTs("srv-px1")) {
				if(lxc.getName().contains("B17")) {
					memOnServer1 += lxc.getMem();

					if(!listCTsrv1.contains(lxc.getVmid())) {
						listCTsrv1.add(lxc.getVmid());
						//s'il y a deja les ct creé avant, on va creer les ct a partir du id prochain
						if((Integer.parseInt(lxc.getVmid())-21700)>=num) {
							num=Integer.parseInt(lxc.getVmid())-21700+1;
						}
					}
				}
			}
			long memOnServer2 = 0;
			// ...
			for (LXC lxc : api.getCTs("srv-px2")) {
				if(lxc.getName().contains("B17")) {
					memOnServer2 += lxc.getMem();
					if(!listCTsrv2.contains(lxc.getVmid())) {
						listCTsrv2.add(lxc.getVmid());
						if((Integer.parseInt(lxc.getVmid())-21700)>=num) {
							num=Integer.parseInt(lxc.getVmid())-21700+1;
						}

					}
				}
			}
			// M�moire autoris�e sur chaque serveur
			float memRatioOnServer1 = memAllowedOnServer1;
			// ...
			float memRatioOnServer2 = memAllowedOnServer2;
			// ... 
			if (memOnServer1 < memRatioOnServer1 && memOnServer2 < memRatioOnServer2) {

				// choisir un serveur al�atoirement avec les ratios sp�cifi�s 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
					serverName = Constants.SERVER1;
				else
					serverName = Constants.SERVER2;

				// cr�er un contenaire sur ce serveur
				idCTcree=(int) (baseID+num);
				api.createCT(serverName,Integer.toString(idCTcree) , "ct-tpgei-ctlv-bB17-ct"+Integer.toString(num), 512);
				
				//mis a jour le memoire total utilise
				if(serverName==Constants.SERVER1) {
					listCTsrv1.add(Integer.toString(idCTcree));
					memOnServer1+=api.getCT(Constants.SERVER1, Integer.toString(idCTcree)).getMem();
				}else {
					listCTsrv2.add(Integer.toString(idCTcree));
					memOnServer2+=api.getCT(Constants.SERVER2, Integer.toString(idCTcree)).getMem();
				}
				
				//incrementer le id du  CT
				num++;
				
				System.out.println("creat sur "+serverName+" id = "+idCTcree);
			
				// planifier la prochaine cr�ation
				int timeToWait =getNextEventPeriodic(lambda); // par exemple une loi peri d'une moyenne de 30sec
				
				// attendre jusqu'au prochain �v�nement
				Thread.sleep(1000 * timeToWait);
				api.startCT(serverName, Integer.toString(idCTcree) );
				
				System.out.println(idCTcree+" lance ");
				
				
				//--- equilibre controlleur
				
				System.out.println("MemUtilse srv1 :"+memOnServer1/1024/1024+"Mb ,MemAllowed srv1 "+memAllowedOnServer1/1024/1024+"Mb\n MemUtilse srv2 :  "+memOnServer2/1024/1024+"Mb , MemAllowed srv2 "+memAllowedOnServer2/1024/1024+"Mb");
				double ratioEnSrv1=(double)memOnServer1/memAllowedOnServer1;
				double ratioEnSrv2=(double)memOnServer2/memAllowedOnServer2;
				
				System.out.println(" ratio srv1 ; srv2 =   "+ ratioEnSrv1 +"  ; "+ratioEnSrv2);
				
				if(ratioEnSrv1>THRESHOLD_EQUILLIBRE && ratioEnSrv1>ratioEnSrv2 ) {
					// migrate le plus ancien CT
					String numct=listCTsrv1.get(0);
					System.out.println(" 1 to 2 migrate ct id = "+numct);
					if(api.getCT(Constants.SERVER1, numct).getStatus().contains("run"))
						api.stopCT(Constants.SERVER1, numct);
					// attendre 
					Thread.sleep(5000);
					api.migrateCT(Constants.SERVER1, numct, Constants.SERVER2);
					Thread.sleep(5000);
					api.startCT(Constants.SERVER2, numct);
					// mis a jour les lists locaux
					listCTsrv1.remove(0);
					listCTsrv2.add(numct);
					System.out.println("migrate srv1 ct fin ");
				}else if(ratioEnSrv2>THRESHOLD_EQUILLIBRE && ratioEnSrv2>ratioEnSrv1 ) {
					// migrate le plus ancien CT
					String numct=listCTsrv2.get(0);
					System.out.println(" 2 to 1 migrate ct id = "+numct);
					if(api.getCT(Constants.SERVER2, numct).getStatus().contains("run"))
						api.stopCT(Constants.SERVER2, numct);
					Thread.sleep(5000);
					api.migrateCT(Constants.SERVER2, numct, Constants.SERVER1);
					Thread.sleep(5000);
					api.startCT(Constants.SERVER1, numct);
					// mis a jour les lists locaux
					listCTsrv2.remove(0);
					listCTsrv1.add(numct);
					System.out.println("migrate srv2 ct fin ");
				}
				
				
				if(ratioEnSrv1>THRESHOLD_STOPCT) {
					for(String s : listCTsrv1) {
						if(api.getCT(Constants.SERVER1, s).getStatus().contains("run")){
							api.stopCT(Constants.SERVER1, s);
							System.out.println(" stop ct sur srv1 "+ s);
							break;
						}
					}
				}
				if(ratioEnSrv2>THRESHOLD_STOPCT) {
					for(String s : listCTsrv2) {
						if(api.getCT(Constants.SERVER2, s).getStatus().contains("run")){
							api.stopCT(Constants.SERVER2, s);
							System.out.println(" stop ct sur srv2 "+ s);
							break;
						}
					}
				}
			}
			else {
				System.out.println("Servers are loaded, waiting 5 more seconds ...");

				Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
			}
			System.out.println("\n \n -------------------------\n\n");
			
		}

	}

}
