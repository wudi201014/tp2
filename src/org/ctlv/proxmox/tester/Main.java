package org.ctlv.proxmox.tester;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Main {

	public static void main(String[] args) throws LoginException, JSONException, IOException {

		ProxmoxAPI api = new ProxmoxAPI();		
		
		List<LXC> cts = api.getCTs("srv-px1");
		
		Long memMaxSrv1=api.getNode(Constants.SERVER1).getMemory_total();
		Long memMaxSrv2=api.getNode(Constants.SERVER2).getMemory_total();
		
		List<LXC> ctsSrv2 = api.getCTs("srv-px2");
		


//		
//		//-----generator
//		long memSrv1=0;
//		long diskSrv1=0;
//		long ratiomem = 0;
//		int num=0;
//		int id=21700;
//		int idCTcree=0;
//		String nomsrv="";
//				
//		while (ratiomem < 0.16) {
//			idCTcree=id+num;
//			Random generator=new Random();
//			if(generator.nextInt(100)<=66) {
//				nomsrv=Constants.SERVER1;
//			}else {
//				nomsrv=Constants.SERVER1;
//			}
//			api.createCT(nomsrv,Integer.toString(idCTcree) , "ct-tpgei-ctlv-bB17-ct"+Integer.toString(num), 512);
//
//			try {
//				Thread.sleep(60000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		//	api.startCT(Constants.SERVER1, Integer.toString(idCTcree));
//			
//			for (LXC lxc : cts) {
//				if(lxc.getName().contains("B17")) {
//				memSrv1 += lxc.getMaxmem();
//				diskSrv1 += lxc.getDisk();
//				}
//			}
//			System.out.println("ratio mem = "+ratiomem);
//			ratiomem=memSrv1/memMaxSrv1;			
//		}
	}
	

}
