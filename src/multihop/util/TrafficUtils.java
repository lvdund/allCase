package multihop.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.util.Vector;
import java.lang.Math;

import multihop.Constants;
import multihop.node.NodeVehicle;
import multihop.request.RequestBase;
import multihop.request.RequestVehicle;

public class TrafficUtils { 
	private static final double e = 2.718281828;

	public static Queue<RequestBase> createReqList(List<NodeVehicle> topo, int testcase) throws IOException {

		Queue<RequestBase> reqPiority = new PriorityQueue<RequestBase>(); // store req by time and id

		int[] numNode = new int[Constants.TSIM];
		FileWriter fileP = new FileWriter("file.csv");
		for (int i = 0 ; i < Constants.TSIM ; i++) {
			numNode[i] = genRandPoisson(20, 1, 20, i, testcase);
			fileP.write(i+","+numNode[i]+"\n");
		}
		// fileP.close();

		// initializing id_node_req
		ArrayList<Integer>[] id_node_req = new ArrayList[Constants.TSIM];
		for (int i = 0; i < Constants.TSIM; i++) {
			id_node_req[i] = new ArrayList<Integer>();
		}

		// get id_node_req
		for( int i=0 ; i < (Constants.TSIM) ; i++ ){
			for(int j = 0; j< numNode[i];j++){
				Integer idNode = IdRequest(1, i, 400, (j + testcase));
				int Duration = Duration(idNode, 1, testcase) % 5;
				for(int k = 0 ; k <= Duration ; k++){
					if( (i + k) >= Constants.TSIM ){
						break;
					}
					while( idNode==100 || idNode==200){
						idNode++;
					}
					if( id_node_req[i+k].contains(idNode) ){
						continue;
					}
					id_node_req[i+k].add(idNode);
				}
			}
		}
		for(int i = 0; i < Constants.TSIM; ++i){
			fileP.write(i+",");
			for (int idNode : id_node_req[i]) {
				fileP.write(idNode+",");
			}
			fileP.write("\n");
		}
		fileP.close();


		// add to reqPriority
		int idReq = 1;
		for (int i = 1; i < (Constants.TSIM-1); i++) {

			// add V1, V2
			reqPiority.add(new RequestBase( idReq, 60, topo.get(100), i, false));
			idReq++;
			reqPiority.add(new RequestBase( idReq, 30, topo.get(200), i, false));
			idReq++;
			
			// for(int j = 101; j < 130; ++j ){
			// 	reqPiority.add(new RequestBase( idReq, 24, topo.get(j), i, false));
			// 	idReq++;
			// }

			if(id_node_req[i] == null){
				continue;
			}
			for (Integer idNode : id_node_req[i]) {
				if(Constants.highPriNode.contains(idNode)){
					reqPiority.add(new RequestBase( idReq, 30, topo.get(idNode), i, false));
				}else{
					reqPiority.add(new RequestBase( idReq, 24, topo.get(idNode), i, false));
				}
				idReq++;
			}
		}

		return reqPiority;
	}

	public static int assignWL(int a, int b, int numberOfWL, int testcase) {
		int c = (a + b + a * b * ((testcase % 6) + 1) + b * (testcase % 7)
				+ (testcase % 4)
						* (1 + (testcase % 5) + 2 * (testcase % 2) * testcase + (testcase % 8) * testcase * testcase))
				% numberOfWL;
		return c;
	}

	public static int IdRequest(int a, int b, int numberOfVehicle, int testcase) {
		int c = (a + b + a * b * ((testcase % 6) + 1) + b * (testcase % 7) * (testcase % 2)
				+ (testcase % 4)
						* (1 + (testcase % 5) + 2 * (testcase % 2) * testcase + (testcase % 8) * testcase * testcase))
				% numberOfVehicle;
		return c;
	}

	public static int Timeinit(int a, int b, int testcase) {
		int c = (a + b + a * b * ((testcase % 4) + 1) + b * (testcase % 9)
				+ (testcase % 4)
						* (1 + (testcase % 5) + 2 * (testcase % 2) * testcase + (testcase % 11) * testcase * testcase))
				% (Constants.TSIM);
		// double d = c / Constants.TSIM * 10;
		// double p = 0;
		// p = c / Constants.TSIM * poisson(Constants.TSIM, Constants.TSIM / 2);
		// int d = calculate1(p, Constants.TSIM);
		return (int)c;
	}

	public static int Duration(int a, int b, int testcase) {
		int c = (a + b + a * b * ((testcase % 6) + 1) + b * (testcase % 7)
				+ (testcase % 4)
						* (1 + (testcase % 5) + 2 * (testcase % 2) * testcase + (testcase % 8) * testcase * testcase))
				% (Constants.TSIM / 5);
		return c;
	}

	public static int numberOfRequest(int a, int b, int testcase) {
		int c = (a + b + a * b * ((testcase % 4) + 1) + b * (testcase % 9)
				+ (testcase % 4)
						* (1 + (testcase % 5) + 2 * (testcase % 2) * testcase + (testcase % 11) * testcase * testcase))
				% (Constants.TSIM);
		double cc = (poisson(20, b)) / poisson(20, 10);
		if (cc < 0)
			cc = -cc;
		double d = (cc * (Constants.TSIM)) % 50;
		int id = (int) d;
		if (b > Constants.TSIM * 3 / 4)
			id = 0;
		System.out.println("\n" + id + "\n");
		return id;
	}

	public static int fact(int i) {
		if (i == 1)
			return 1;
		else
			return i * fact(i - 1);
	}

	public static int genRandPoisson(double lambda, double time, int numReq, int testcase, int test) {

		int n = 0;
		int m = Timeinit(1, test, testcase);
		double u = (double) m / Constants.TSIM;
		double el = Math.exp(-(lambda * time));
		double pp = Math.exp(-(lambda * time));
		double fact = 1.0;
		double pow = 1.0;
		while (u > pp) {
			n = n + 1;
			fact = fact * n;
			pow = lambda * pow;
			pp = pp + (pow / fact) * el;
		}
		int mm = n + (n - 20) / 2;
		return Math.abs(mm);
	}

	public static int CheckNode(int array[], int n, int idNode) {
		int check = 0;
		for (int i = 0; i < n; i++) {
			if (idNode == array[i]) {
				idNode++;
				idNode=idNode%400;
				return CheckNode(array, n, idNode);
			}
		}
		return idNode;
	}
	
	public static double poisson(double lamda, int key) {
		// double a = Math.pow(e, -lamda);
		double cc = key;
		double b = Math.pow(lamda, cc);
		int c = 1;
		for (int i = 1; i <= key; i++) {
			c = c * i;
		}
		return (b / c);
	}

	public static int calculate1(double p, double lamda) {
		int a = 2, check = 1;
		for (int i = 0; i < lamda; i++) {
			if (p < poisson(lamda, i)) {
				a = i;
				check = 0;
			}
			if (check == 0) {
				break;
			}
		}
		return a;
	}

}
