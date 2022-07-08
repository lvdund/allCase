package multihop.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.util.Vector;

import multihop.Constants;
import multihop.node.NodeVehicle;
import multihop.request.RequestBase;
import multihop.request.RequestVehicle;

public class TrafficUtils {
	private static final double e = 2.718281828;

	public static Queue<RequestBase> createReqList(List<NodeVehicle> topo) throws IOException {
		FileWriter traff;
		traff = new FileWriter("traffic_module.txt");
		traff.write("\n List REQ: \n" + "id\t" + "srcNode\t" + "timeArrival\t" + "workLoad\t" + "\n");

		Queue<RequestBase> reqPiority = new PriorityQueue<RequestBase>(); // store req by time and id

		int[] data = { 24, 30, 60, 90, 120, 150, 200, 300, 500 };
		// int[] data = { 300, 300, 300, 300 };
		// int[] data = { 24, 30, 60, 100, 200, 80 };
		int DATA = 0;
		// int[] fixedNode = { 6, 0, 5 };
		// int[] fixedNode = { 11, 9, 8, 7, 10 };
		// int[] fixedNode = { 1, 8, 5, 6, 9, 15, 25, 64, 32, 45, 39, 71, 76, 85, 43 };
		// int[] fixedNode = { 4, 6, 9, 11 };
		int[] fixedNode = { 2, 4, 0 };

		int idReq = 1;
		for (int n = 0; n < fixedNode.length; n++) {
			int idNode = fixedNode[n];
			double reqTime = 0;
			for (int i = 1; i <= Constants.NUM_REQ[1]; i++) {
				int wl = data[DATA];
				Random generator = new Random();
				int ia = assignWL(1, idReq, 3); // get WL
				wl = data[ia];

				// if (idNode == 6) {wl = data[3];} else {wl = data[3];}
				reqPiority.add(new RequestBase(idReq, wl, topo.get(idNode), reqTime, false));
				traff.write(idReq + "\t" + idNode + "\t" + reqTime + "\t" + wl + "\n");
				reqTime += 1; // == timeArrival
				idReq++;
			}
		}
		traff.close();
		return reqPiority;
	}

	public static int assignWL(int a, int b, int numberOfWL) {
		int c = (a + b) % numberOfWL;
		return c;
	}

	// public static Queue<RequestBase> createReqList_Possion(List<NodeVehicle>
	// topo) throws IOException {
	// FileWriter traff;
	// Queue<RequestBase> reqPiority = new PriorityQueue<RequestBase>(); // store
	// req by time and id

	// }

	public static int fact(int i) {
		// base case is i = 0
		if (i == 0)
			return 1; // return 1 when base case is reached
		else
			return i * fact(i - 1); // i is decreased by 1 until i becomes 0
	}

	public double calculate(double mean, int count) {
		double pdf = 0;
		int i = count;
		// the denominator of the formula is x factorial
		int denominator = fact(i);

		double numerator = Math.pow(e, -mean) * Math.pow(mean, count);

		// the pdf value is found based on the formula
		pdf = numerator / denominator;

		return pdf; // return pdf calculated in order to find cdf
	}
}
