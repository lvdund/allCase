package multihop;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collector;
import java.util.Map.Entry;

import PSOSim.PSOSwarm;
import PSOSim.PSOVector;
import multihop.Constants.TYPE;
import multihop.node.NodeBase;
import multihop.node.NodeRSU;
import multihop.node.NodeVehicle;
import multihop.node.NodeCloud;
import multihop.request.RequestBase;
import multihop.request.RequestRSU;
import multihop.request.RequestVehicle;
import multihop.request.RequestCloud;
import multihop.util.AlgUtils;
import multihop.util.TopoUtils;
import multihop.util.TrafficUtils;

public class MainSim {

	static int TS = Constants.TS; // =1

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

		/**
		 * ------------------------------- Prams-------------------------------
		 **/
		boolean DEBUG = true;
		boolean single;
		int hc = 1; // hop-count is number of path to a node

		/**
		 * --- 1.Create topology---
		 */

		// vehicle node
		int _m = 20, _n = 20;
		List<NodeVehicle> topo = new ArrayList<NodeVehicle>();
		topo = (List<NodeVehicle>) TopoUtils.createTopo(_m, _n, 50, Constants.TYPE.VEHICLE.ordinal());
		TopoUtils.updateTimeTopo(topo); // adding moving by time for Vehicle

		// RSU node
		_m = 6;
		_n = 6;
		List<NodeRSU> topoRSU = new ArrayList<NodeRSU>();
		topoRSU = (List<NodeRSU>) TopoUtils.createTopo(_m, _n, 200, Constants.TYPE.RSU.ordinal());

		// make connection child-parent-neighbor

		// CLoud node - 1 cloud
		_m = 1;
		_n = 1;
		List<NodeCloud> topoCloud = new ArrayList<NodeCloud>();
		topoCloud = (List<NodeCloud>) TopoUtils.createTopo(_m, _n, 50, Constants.TYPE.SERVER.ordinal());

		for (int nodeHighPri : Constants.highPriNode) {
			topo.get(nodeHighPri).setPri(true);
		}

		int xx = 3;

		for (int iac = xx; iac <= xx; iac++) {
			
			/**
			 * --- 2. Create N requests --- id workload request node time_start=time_arrival
			 */
			Queue<RequestBase> reqPiority = new PriorityQueue<RequestBase>(); // store req by time and id
			reqPiority = TrafficUtils.createReqList(topo, iac);

			FileWriter myWriterPSO;
			myWriterPSO = new FileWriter("Result-testcase " + iac + " Duration 5s" + ".csv");

			// transfer queue to list (cz index in queue isn't right): sắp xếp theo thời gian -- file ff_module
			List<RequestBase> req = new ArrayList<RequestBase>();
			while (reqPiority.size() != 0) {
				req.add(reqPiority.poll());
			}

			TopoUtils.setupTopoRSU(topoRSU, topo); // create neighbor
			TopoUtils.setupTopo(topo, topoRSU); // create neighbor

			int nTS = Constants.TSIM;
			/**
			 * Loop ts: 0 -- ts_1 -- ts_1 + TS
			 * final int nTS = Constants.TSIM - 1; // nTS = 14
			 */

			int testCase = 0;

			int[] testCaseList = { 1 }; // {1, 2, 6, 7 }
			for (Integer test : testCaseList) {
				testCase = test;
				for (int h = 1; h <= 1; h++) { // hopcount = 1 or 2
					hc = h; // hc = 1
					int opts = h == 1 ? 1 : 2; // hc=1 -> 1 opts; hc=2 -> 2 opts
					// opts =1;

					System.out.println("CASE NO CAP");

					for (int s = 0; s < opts; s++) {
						
						single = s == 0 ? true : false; // single and multi opts
						
						// // Case: Vehicle Only no cap + RSU no cap

						// clear node
						for (NodeVehicle n : topo) { // ------------------
							n.getqReq().clear();
							n.getDoneReq().clear();
							n.setcWL(0);
							n.setaWL(0);
							// n.setpWL(0);
						}

						// single =false;

						String o = single == true ? "s" : "m";

						for (int t = 1; t <= nTS; t++) {
							System.out.println("\nts= " + t	+ " ---------------------------------------------------------------------------------------------");
							double ts = TS * t;

							// print - begin ts = 1 to 14

							/**
							 * --- 1. Create routing table---
							 */

							HashMap<Integer, List<RTable>> mapRTable = new HashMap<Integer, List<RTable>>();
							List<RTable> rtable = new ArrayList<RTable>();

							// 1.1 prepare request to node in: ts_k < start < ts_k+1
							// debug("List REQs: ( prepare req to node )\n ", DEBUG);
							List<NodeVehicle> listNodeReq = new ArrayList<NodeVehicle>(); // node having reqs in ts
							Queue<RequestBase> reqTS = new PriorityQueue<RequestBase>(); // reqTS having in ts

							for (RequestBase r : req) {
								double start = r.getTimeInit();
								if ((start < ts) && (start >= (ts - TS))) {
									listNodeReq.add(r.getSrcNode());
									reqTS.add(r);
								}
							}

							// 1.2 updated create routing-table:rtable and routing-table-with-id:mapRTable with requests
							for (RequestBase r : reqTS) {
								List<RTable> rtableREQ = new ArrayList<RTable>(); // rtable of a request
								int reqId = r.getId();
								// NodeVehicle reqNode = r.getSrcNode();
								// double WL = r.getWL();
								rtableREQ = TopoUtils.createRoutingTable(topo, rtableREQ, r, listNodeReq, hc, single, t);
								rtable.addAll(rtableREQ); // merge all reqs
								mapRTable.put(reqId, rtableREQ); // merge reqs with id
							}

							for (int i = 0; i < rtable.size() - 1; i++) {
								for (int j = i + 1; j < rtable.size() - 1; j++) {
									if (rtable.get(i).getDes().equals(rtable.get(j).getDes())) {
										if (!rtable.get(i).getDes().equals(rtable.get(i).getRoute())) {
											if (!rtable.get(j).getDes().equals(rtable.get(j).getRoute())) {
												int in = rtable.get(i).getsameDes() + 1;
												int im = rtable.get(j).getsameDes() + 1;
												rtable.get(i).setsameDes(in);
												rtable.get(j).setsameDes(im);
											}
										}
									}
								}
							}
							for (int i = 0; i < rtable.size(); i++) {
								double maxWL = 0;
								for (int j = 0; j < rtable.size(); j++) {
									if (rtable.get(i).getReq() == rtable.get(j).getReq()) {
										maxWL += rtable.get(j).getResource() / rtable.get(j).getsameDes();
									}
								}
								// if (rtable.get(i).getReq().getWL() > maxWL) {
									// rtable.get(i).setDecrease(rtable.get(i).getDecrease()+1);
									// rtable.get(i).getDesNode().setDecreaseGain();
								// } else {
									// rtable.get(i).setDecrease(0);
									// rtable.get(i).getDesNode().setDecreaseZero();
								// }
							}

							// TODO: create routing-table for RSU

							/**
							 * --- 2. Run PSO ---
							 */

							// if having req in queue

							if (listNodeReq.size() != 0) {

								// System.out.println("\n***********PSO Running***********\n");

								HashMap<Integer, Double> resultPSO = AlgUtils.getPSO(rtable, mapRTable, testCase, ts);

								Set<Integer> rID = resultPSO.keySet();
								for (Integer id : rID) {
									rtable.get(id).setRatio(resultPSO.get(id));
								}

								/**
								 * --- 3. Logging ---
								 */
								calcTSerPSO(rtable, testCase);

							}
							/**
							 * --- 4. Process CWL and queue in node ---
							 * 
							 */
							// 4.1 assigned workload in node (all cWL) and adding queue
							insertQ(topo, rtable, t);

							// 4.2 update queue
							updateQ(topo, ts);

							// 4.2 calculate cWL
							updateCWL(topo, ts);

							// 4.3 moving to cloud

							HashMap<Integer, List<RTable>> mapRTableRSU = new HashMap<Integer, List<RTable>>();
							List<RTable> rtableRSU = new ArrayList<RTable>();

							// 1.1 prepare request to node RSU base: moving-data

							// debug("List REQs--------------:\n ", DEBUG);
							List<NodeRSU> listNodeReqRSU = new ArrayList<NodeRSU>(); // node having reqs in ts
							Queue<RequestRSU> reqTSRSU = new PriorityQueue<RequestRSU>(); // reqTS having in ts

							// list node moving data and the requestID

							for (NodeRSU n : topoRSU) {
								if (n.getqReqV().peek() != null) {
									listNodeReqRSU.add(n);
								}
							}

							// list request in a TS
							for (NodeRSU n : topoRSU) {
								for (RequestRSU rr : n.getqReqV()) { // multi req to 1 node
									rr.setSrcNodeRSU(n);
									reqTSRSU.add(rr);
								}
							}

							for (RequestRSU r : reqTSRSU) {
								List<RTable> rtableREQ = new ArrayList<RTable>(); // rtable of a request
								int reqId = r.getId();
								rtableREQ = TopoUtils.createRoutingTableRSU(topoRSU, r, listNodeReqRSU, hc, single, t);
								rtableRSU.addAll(rtableREQ); // merge all reqs
								mapRTableRSU.put(reqId, rtableREQ); // merge reqs with id
							}

							// System.out.println("\n***********PSO Running RSU***********\n");

							HashMap<Integer, Double> resultPSORSU = AlgUtils.getPSORSU(rtableRSU, mapRTableRSU, testCase, ts);

							// get output of PSO-alg
							Set<Integer> rID = resultPSORSU.keySet();
							for (Integer id : rID) {
								rtableRSU.get(id).setRatio(resultPSORSU.get(id));
							}

							int i = 0;
							int[] id = new int[reqTSRSU.size()];
							for (RequestRSU rr : reqTSRSU) {
								double max = 0;
								int i2 = 0;
								for (RTable r : rtableRSU) {
									if (r.getReq().getId() == rr.getId()) {
										if (r.getRatio() > max) {
											max = r.getRatio();
											id[i] = i2;
										}
										r.setRatio(0); // set all to 0
									}
									i2++;
								}
								i++;
							}

							for (Integer i2 : id) {
								rtableRSU.get(i2).setRatio(1); // set max to 1
							}

							// TODO create

							calcTSerPSORSU(rtableRSU, testCase);

							insertQRSU(topoRSU, rtableRSU, t, topoCloud);

							updateQRSU(topoRSU, ts);

							updateCWLRSU(topoRSU, ts);

						} // endts

						myWriterPSO.write("ReqID," + "TimeInit," + "a(SrcNode)," + "i(DesNode)," + "k(Path),"
								+ " Work load," + "p(Ratio)," + "\n");
						for (RequestBase r : req) {
							for (NodeVehicle n : topo) {
								for (RequestBase d : n.getDoneReq()) {
									if (d.getId() == r.getId()) {
										if (n.getName().equals(r.getSrcNode().getName())) {
											RequestVehicle dv = (RequestVehicle) d;
											myWriterPSO.write(
													r.getId() + "," + dv.getTimeInit() + "," + r.getSrcNode().getName()
															+ ","
															+ r.getSrcNode().getName()
															+ "," + r.getSrcNode().getName() + "," + r.getWL() + ","
															+ DecimalFormat1(1) + ",\n");
										}
									}
								}
							}
						}

						myWriterPSO.write("Vehicle Only no cap");
						myWriterPSO.write("\nReqID," + "timeInit," + "a(SrcNode)," + "i(DesNode)," + "k(Path),"
								+ "Work load," + "p(Ratio)," + "Assign Work load,"
								+ "dtTrans," + "tArrival," + "t_wait," + "start," + "t_proc," + "end," + "t_serv(PSO),"
								+ "t_serv(each Vehicle)," + "t_serv(all),"
								+ "moved_data" + "\n");
						double wait = 0;
						int count = 0;

						// log for vehicle topo
						for (RequestBase r : req) {
							double endM = 0;
							for (NodeVehicle n : topo) {
								for (RequestBase d : n.getDoneReq()) {
									if (d.getId() == r.getId()) {
										endM = endM > ((RequestVehicle) d).getEnd() ? endM
												: ((RequestVehicle) d).getEnd();
										wait += (((RequestVehicle) d).getStart()
												- ((RequestVehicle) d).getTimeArrival());
										count++;
									}
								}
							}

							// endM -= Math.floor(((RequestVehicle) r).getTimeArrival());
							endM = endM - r.getTimeInit();
							if (endM < 0) {
								endM = 0;
							}

							for (NodeVehicle n : topo) {
								for (RequestBase d : n.getDoneReq()) {
									if (d.getId() == r.getId()) {

										RequestVehicle dv = (RequestVehicle) d;
										double t_wait = dv.getStart() - dv.getTimeArrival();
										double t_proc = dv.getEnd() - dv.getStart();
										double t_serv = dv.getTimeTrans() + t_wait + t_proc;
										myWriterPSO.write(r.getId() + "," + DecimalFormat1(r.getTimeInit()) + ","
												+ dv.getSrcNode().getName() + ","
												+ n.getName()
												+ "," + dv.getRoute() + "," + dv.getWL() + ","
												+ (DecimalFormat1(dv.getRatio())) + ","
												+ (DecimalFormat1(dv.getRatio() * dv.getWL())) + ","
												+ DecimalFormat1(dv.getTimeTrans())
												+ "," + DecimalFormat1(dv.getTimeArrival()) + ","
												+ DecimalFormat1(t_wait)
												+ "," + DecimalFormat1(dv.getStart()) + ","
												+ DecimalFormat1(t_proc) + "," + DecimalFormat1(dv.getEnd()) + ","
												+ DecimalFormat1(dv.getTimeSer()) + ","
												+ DecimalFormat1(t_serv)
												+ "," + DecimalFormat1(endM)
												+ "," + DecimalFormat1(0) + "\n");
									}
								}
							}
						}

						myWriterPSO.write("ReqID," + "TimeInit," + "a(SrcNode)," + "i(DesNode)," + "k(Path),"
								+ " Work load," + "t_serv" + "\n");
						for (RequestBase r : req) {
							double endM = 0;
							for (NodeVehicle n : topo) {
								for (RequestBase d : n.getDoneReq()) {
									if (d.getId() == r.getId()) {
										endM = endM > ((RequestVehicle) d).getEnd() ? endM
												: ((RequestVehicle) d).getEnd();
										wait += (((RequestVehicle) d).getStart()
												- ((RequestVehicle) d).getTimeArrival());
										count++;
									}
								}
							}

							// endM -= Math.floor(((RequestVehicle) r).getTimeArrival());
							endM = endM - r.getTimeInit();
							if (endM < 0) {
								endM = 0;
							}

							for (NodeVehicle n : topo) {
								for (RequestBase d : n.getDoneReq()) {
									if (d.getId() == r.getId()) {
										if (n.getName().equals(r.getSrcNode().getName())) {
											RequestVehicle dv = (RequestVehicle) d;
											myWriterPSO.write(
													r.getId() + "," + dv.getTimeInit() + "," + r.getSrcNode().getName()
															+ "," + r.getSrcNode().getName()
															+ "," + r.getSrcNode().getName() + "," + r.getWL() + ","
															+ DecimalFormat1(endM) + "\n");
										}
									}

								}
							}
						}

						myWriterPSO.write("RSU Only no cap\n");

						myWriterPSO.write("ReqID," + "TimeInit," + "a(SrcNode)," + "i(DesNode)," + "k(Path),"
								+ "Work load,"
								+ "t_serv(vehicle)," + "moved_data_toRSU,");

						myWriterPSO.write("Vehicle->RSU," + "a(SrcNode)," + "i(DesNode)," + "k(Path)," + "p(Ratio),"
								+ "t_trans_v2r," + "dtTrans(R2R)," + "t_Arrival(RSU)," + "t_wait(RSU),"
								+ "t_start(RSU)," + "t_proc(RSU),"
								+ "t_end(RSU),"
								+ "t_Ser(PSO),"
								+ "t_serv(RSUlayer),"
								+ "t_service(RSU)," + "moved_data_to_Cloud," + "Tserv(all),\n");

						for (RequestBase r : req) {
							double endM = 0;
							double endM1 = 0;
							double t_all_RSU = 0;
							double t_all_Cloud = 0;
							for (NodeVehicle n : topo) {
								for (RequestBase d : n.getDoneReq()) {
									if (d.getId() == r.getId()) {
										endM = endM > ((RequestVehicle) d).getEnd() ? endM
												: ((RequestVehicle) d).getEnd();
										wait += (((RequestVehicle) d).getStart()
												- ((RequestVehicle) d).getTimeArrival());
										count++;
									}
								}
							}

							// endM -= Math.floor(((RequestVehicle) r).getTimeArrival());
							endM = endM - r.getTimeInit();
							if (endM < 0) {
								endM = 0;
							}

							for (NodeRSU n : topoRSU) {
								for (RequestBase d : n.getDoneReq()) {
									if (d.getId() == r.getId()) {
										endM1 = endM1 > ((RequestRSU) d).getEnd() ? endM1 : ((RequestRSU) d).getEnd();
										wait += (((RequestRSU) d).getStart() - ((RequestRSU) d).getTimeArrival());
										count++;
									}
								}
							}

							// endM -= Math.floor(((RequestVehicle) r).getTimeArrival());
							endM1 -= Math.floor(r.getTimeInit());
							if (endM1 < 0) {
								endM1 = 0;
							}

							for (NodeVehicle n : topo) {
								for (RequestBase d : n.getDoneReq()) {
									RequestVehicle dv = (RequestVehicle) d;
									if (d.getId() == r.getId()) {
										if (d.getSrcNode().getName().equals(n.getName())) {
											double t_wait = dv.getStart() - dv.getTimeArrival();
											double t_proc = dv.getEnd() - dv.getStart();
											double t_serv = dv.getTimeTrans() + t_wait + t_proc;
											myWriterPSO.write(r.getId() + "," + dv.getTimeInit() + ","
													+ dv.getSrcNode().getName() + ","
													+ n.getName()
													+ "," + dv.getRoute() + "," + dv.getWL() + ","
													+ DecimalFormat1(0)
													+ "," + DecimalFormat1(dv.getMovedData()));
										}
									}
								}
							}
							for (NodeRSU n : topoRSU) {
								for (RequestBase d : n.getDoneReq()) {
									if (d.getId() == r.getId()) {
										RequestRSU dv = (RequestRSU) d;
										double t_wait = dv.getStart() - dv.getTimeArrival();
										double t_proc = dv.getEnd() - dv.getStart();
										double t_serv = dv.getTimeTrans() + dv.getTimeVR() + t_wait + t_proc;
										double t_trans_vr = dv.getTimeVR();
										t_all_RSU = dv.getEnd() - r.getTimeInit();

										if (t_proc == 0) {
											t_all_RSU = 0;
											dv.setTimeArrival(0);
											dv.setStart(0);
											dv.setTimeSer(0);
											dv.setEnd(0);
											dv.setTimeVR(0);
											t_trans_vr = 0;
											t_wait = 0;
											t_serv = 0;
											endM1 = 0;
										}

										myWriterPSO.write(",Vehicle->RSU" + ","
												+ dv.getSrcNodeRSU().getName() + "," + n.getName() + "," + dv.getRoute()
												+ "," + DecimalFormat1(dv.getRatio()) + ","
												+ DecimalFormat1(t_trans_vr) + "," + DecimalFormat1(dv.getTimeTrans()) + ","
												+ DecimalFormat1(dv.getTimeArrival()) + "," + DecimalFormat1(t_wait) + ","
												+ DecimalFormat1(dv.getStart()) + ","
												+ DecimalFormat1(t_proc) + ","
												+ DecimalFormat1(dv.getEnd()) + ","
												+ DecimalFormat1(t_serv) + "," + DecimalFormat1(t_serv) + ","
												+ DecimalFormat1(endM1) + "," 
												+ DecimalFormat1(dv.getMovedData()) 
												+ "," + DecimalFormat1(endM1) + "\n");
									}
								}
							}
						}
						
						// RESET

						// //////////////////////////////////////////////////////////////////////

						for (NodeVehicle n : topo) { // ------------------
							n.getqReq().clear();
							n.getDoneReq().clear();
							n.setcWL(0);
							n.setaWL(0);
							n.getDecrease().clear();
							// n.setpWL(0);
						}

						for (NodeRSU n : topoRSU) { // ------------------
							n.getqReq().clear();
							n.getDoneReq().clear();
							n.setCWL(0);
							n.setaWL(0);
							// n.setpWL(0);
						}

						System.out.println("CASE NORMAL");

						// Case: RSU and Vehicle

						for (int t = 1; t <= nTS; t++) {
							System.out.println("\nts= " + t + " --------------------------------------------------------");
							double ts = TS * t;

							// print - begin ts = 1 to 14

							/**
							 * --- 1. Create routing table---
							 */

							HashMap<Integer, List<RTable>> mapRTable = new HashMap<Integer, List<RTable>>();
							List<RTable> rtable = new ArrayList<RTable>();

							// 1.1 prepare request to node in: ts_k < start < ts_k+1
							// debug("List REQs: ( prepare req to node )\n ", DEBUG);
							List<NodeVehicle> listNodeReq = new ArrayList<NodeVehicle>(); // node having reqs in ts
							Queue<RequestBase> reqTS = new PriorityQueue<RequestBase>(); // reqTS having in ts

							for (RequestBase r : req) {
								double start = r.getTimeInit();
								if ((start < ts) && (start >= (ts - TS))) {
									listNodeReq.add(r.getSrcNode());
									reqTS.add(r);
								}
							}

							// 1.2 updated create routing-table:rtable and routing-table-with-id:mapRTable with requests
							for (RequestBase r : reqTS) {
								List<RTable> rtableREQ = new ArrayList<RTable>(); // rtable of a request
								int reqId = r.getId();
								// NodeVehicle reqNode = r.getSrcNode();
								// double WL = r.getWL();
								rtableREQ = TopoUtils.createRoutingTable(topo, rtableREQ, r, listNodeReq, hc, single,
										t);
								rtable.addAll(rtableREQ); // merge all reqs
								mapRTable.put(reqId, rtableREQ); // merge reqs with id
							}

							// TODO: create routing-table for RSU

							/**
							 * --- 2. Run PSO ---
							 */

							// if having req in queue

							if (listNodeReq.size() != 0) {

								HashMap<Integer, Double> resultPSO = AlgUtils.getPSO(rtable, mapRTable, testCase, ts);

								Set<Integer> rID = resultPSO.keySet();
								for (Integer id : rID) {
									rtable.get(id).setRatio(resultPSO.get(id));
								}
								
								/**
								 * --- 3. Logging ---
								 */
								calcTSerPSO(rtable, testCase);

							}
							/**
							 * --- 4. Process CWL and queue in node ---
							 */
							// 4.1 assigned workload in node (all cWL) and adding queue

							insertQ1(topo, rtable, t);

							// 4.2 update queue
							updateQ(topo, ts);

							// 4.2 calculate cWL
							updateCWL(topo, ts);

							// 4.3 moving to cloud

							HashMap<Integer, List<RTable>> mapRTableRSU = new HashMap<Integer, List<RTable>>();
							List<RTable> rtableRSU = new ArrayList<RTable>();

							// 1.1 prepare request to node RSU base: moving-data

							// debug("List REQs--------------:\n ", DEBUG);
							List<NodeRSU> listNodeReqRSU = new ArrayList<NodeRSU>(); // node having reqs in ts
							Queue<RequestRSU> reqTSRSU = new PriorityQueue<RequestRSU>(); // reqTS having in ts

							// list node moving data and the requestID

							for (NodeRSU n : topoRSU) {
								if (n.getqReqV().peek() != null) {
									listNodeReqRSU.add(n);
								}
							}

							// list request in a TS
							for (NodeRSU n : topoRSU) {
								for (RequestRSU rr : n.getqReqV()) { // multi req to 1 node
									rr.setSrcNodeRSU(n);
									reqTSRSU.add(rr);
								}
							}

							for (RequestRSU r : reqTSRSU) {
								List<RTable> rtableREQ = new ArrayList<RTable>(); // rtable of a request
								int reqId = r.getId();
								rtableREQ = TopoUtils.createRoutingTableRSU(topoRSU, r, listNodeReqRSU, hc, single, t);
								rtableRSU.addAll(rtableREQ); // merge all reqs
								mapRTableRSU.put(reqId, rtableREQ); // merge reqs with id
							}

							// System.out.println("\n***********PSO Running RSU***********\n");

							HashMap<Integer, Double> resultPSORSU = AlgUtils.getPSORSU(rtableRSU, mapRTableRSU,
									testCase,
									ts);

							// get output of PSO-alg
							Set<Integer> rID = resultPSORSU.keySet();
							for (Integer id : rID) {
								rtableRSU.get(id).setRatio(resultPSORSU.get(id));
							}

							int i = 0;
							int[] id = new int[reqTSRSU.size()];
							for (RequestRSU rr : reqTSRSU) {
								double max = 0;
								int i2 = 0;
								for (RTable r : rtableRSU) {
									if (r.getReq().getId() == rr.getId()) {
										if (r.getRatio() > max) {
											max = r.getRatio();
											id[i] = i2;
										}
										r.setRatio(0); // set all to 0
									}
									i2++;
								}
								i++;
							}

							//

							for (Integer i2 : id) {
								rtableRSU.get(i2).setRatio(1); // set max to 1
							}

							// done set ratio to rtable.

							// TODO create

							calcTSerPSORSU(rtableRSU, testCase);

							insertQRSU(topoRSU, rtableRSU, t, topoCloud);

							updateQRSU(topoRSU, ts);

							updateCWLRSU(topoRSU, ts);

						} // endts
					}

					myWriterPSO.write("RSU and Vehicle\n");

					myWriterPSO.write("ReqID," + "TimeInit," + "a(SrcNode)," + "i(DesNode)," + "k(Path),"
							+ "Work load,"
							+ "t_serv(vehicle)," + "moved_data_toRSU,");

					myWriterPSO.write("Vehicle->RSU," + "a(SrcNode)," + "i(DesNode)," + "k(Path)," + "p(Ratio),"
							+ "t_trans_v2r," + "dtTrans(R2R)," + "t_Arrival(RSU)," + "t_wait(RSU),"
							+ "t_start(RSU)," + "t_proc(RSU),"
							+ "t_end(RSU),"
							+ "t_Ser(PSO),"
							+ "t_serv(RSUlayer),"
							+ "t_service(RSU)," + "moved_data_to_Cloud," + "Tserv(all)\n");

					double count = 0;
					double wait = 0;

					for (RequestBase r : req) {
						double endM = 0;
						double endM1 = 0;
						double t_all_RSU = 0;
						double t_all_Cloud = 0;
						for (NodeVehicle n : topo) {
							for (RequestBase d : n.getDoneReq()) {
								if (d.getId() == r.getId()) {
									endM = endM > ((RequestVehicle) d).getEnd() ? endM : ((RequestVehicle) d).getEnd();
									wait += (((RequestVehicle) d).getStart() - ((RequestVehicle) d).getTimeArrival());
									count++;
								}
							}
						}

						// endM -= Math.floor(((RequestVehicle) r).getTimeArrival());
						endM = endM - r.getTimeInit();
						if (endM < 0) {
							endM = 0;
						}

						for (NodeRSU n : topoRSU) {
							for (RequestBase d : n.getDoneReq()) {
								if (d.getId() == r.getId()) {
									endM1 = endM1 > ((RequestRSU) d).getEnd() ? endM1 : ((RequestRSU) d).getEnd();
									wait += (((RequestRSU) d).getStart() - ((RequestRSU) d).getTimeArrival());
									count++;
								}
							}
						}

						// endM -= Math.floor(((RequestVehicle) r).getTimeArrival());
						endM1 -= Math.floor(r.getTimeInit());
						if (endM1 < 0) {
							endM1 = 0;
						}

						for (NodeVehicle n : topo) {
							for (RequestBase d : n.getDoneReq()) {
								RequestVehicle dv = (RequestVehicle) d;
								if (d.getId() == r.getId()) {
									if (d.getSrcNode().getName().equals(n.getName())) {
										double t_wait = dv.getStart() - dv.getTimeArrival();
										double t_proc = dv.getEnd() - dv.getStart();
										double t_serv = dv.getTimeTrans() + t_wait + t_proc;
										myWriterPSO.write(r.getId() + "," + dv.getTimeInit() + ","
												+ dv.getSrcNode().getName() + ","
												+ n.getName()
												+ "," + dv.getRoute() + "," + dv.getWL() + ","
												+ endM
												+ "," + dv.getMovedData());
									}
								}

							}

						}
						for (NodeRSU n : topoRSU) {
							for (RequestBase d : n.getDoneReq()) {
								if (d.getId() == r.getId()) {
									RequestRSU dv = (RequestRSU) d;
									double t_wait = dv.getStart() - dv.getTimeArrival();
									double t_proc = dv.getEnd() - dv.getStart();
									double t_serv = dv.getTimeTrans() + dv.getTimeVR() + t_wait + t_proc;
									double t_trans_vr = dv.getTimeVR();
									t_all_RSU = dv.getEnd() - r.getTimeInit();

									if (t_proc == 0) {
										t_all_RSU = 0;
										dv.setTimeArrival(0);
										dv.setStart(0);
										dv.setTimeSer(0);
										dv.setEnd(0);
										dv.setTimeVR(0);
										t_trans_vr = 0;
										t_wait = 0;
										t_serv = 0;
										endM1 = 0;
									}

									myWriterPSO.write(",Vehicle->RSU,"
											+ dv.getSrcNodeRSU().getName() + "," + n.getName() + "," + dv.getRoute()
											+ "," + DecimalFormat1(dv.getRatio()) + ","
											+ DecimalFormat1(t_trans_vr) + "," + DecimalFormat1(dv.getTimeTrans()) + ","
											+ DecimalFormat1(dv.getTimeArrival()) + "," + DecimalFormat1(t_wait) + ","
											+ DecimalFormat1(dv.getStart()) + ","
											+ DecimalFormat1(t_proc) + ","
											+ DecimalFormat1(dv.getEnd()) + ","
											+ DecimalFormat1(t_serv) + "," + DecimalFormat1(t_serv) + ","
											+ DecimalFormat1(endM1) + ","
											+ DecimalFormat1(dv.getMovedData()) + ","
											+ DecimalFormat1(Max(endM, endM1, 0)) + "\n");
								}
							}
						}

						// double k = Max(endM, t_all_Cloud, endM1);
					}
					myWriterPSO.close();

				}

				System.out.println("----FINISH-----");
			}

		}
	}

	private static void updateCWL(List<NodeVehicle> topo, double ts) {
		// System.out.println("\n----- Current WL: ");
		// listCWL.write("p");
		for (NodeVehicle n : topo) {
			double pWL = 0; // processed workload
			// double aWL = 0; // all assigned worload
			if ((n.getqReq().size() != 0) || n.getDoneReq().size() != 0) { // node in processing

				for (RequestBase d : n.getDoneReq()) {
					pWL += (((RequestVehicle) d).getEnd() - ((RequestVehicle) d).getStart()) * n.getRes();
				}

				if (n.getqReq().peek() != null) {
					double lastStart = ((RequestVehicle) n.getqReq().peek()).getStart();
					if (lastStart < ts) {
						pWL += (ts - lastStart) * n.getRes();
					}
				}
				// System.out.print("Process WL: " + pWL + " / " + n.getaWL());
				n.setcWL((n.getaWL() - pWL) < 0 ? 0 : (n.getaWL() - pWL));
				// System.out.println("\tcWL: " + n.getcWL());
			}
		}
	}

	private static void updateCWLRSU(List<NodeRSU> topoRSU, double ts) {
		// System.out.println("\n----- Current WL: ");
		// listCWL.write("p");
		for (NodeRSU n : topoRSU) {
			double pWL = 0; // processed workload
			// double aWL = 0; // all assigned worload
			if ((n.getqReq().size() != 0) || n.getDoneReq().size() != 0) { // node in processing

				for (RequestBase d : n.getDoneReq()) {
					pWL += (((RequestRSU) d).getEnd() - ((RequestRSU) d).getStart()) * n.getRes();
				}

				if (n.getqReq().peek() != null) {
					double lastStart = ((RequestRSU) n.getqReq().peek()).getStart();
					if (lastStart < ts) {
						pWL += (ts - lastStart) * n.getRes();
					}
				}
				// System.out.print("Process WL: " + pWL + " / " + n.getaWL());
				n.setCWL((n.getaWL() - pWL) < 0 ? 0 : (n.getaWL() - pWL));
				// System.out.println("\tcWL: " + n.getcWL());
			}

		}
	}

	private static void updateQ(List<NodeVehicle> topo, double ts) {
		// System.out.println("\nUPDATE QUEUE");
		for (NodeVehicle n : topo) {
			boolean check = true;
			while (check && (n.getqReq().peek() != null)) { // còn queue
				RequestVehicle rv = (RequestVehicle) n.getqReq().peek();
				double start1 = rv.getStart();
				double end1 = rv.getEnd();
				check = false;
				if (end1 < ts) {
					n.getqReq().peek().setDone(true);
					n.getDoneReq().add(n.getqReq().peek()); // adding to done req
					// System.out.println("doneREQ: " + rv.getSrcNode().getName() + " : " + rv.getMovedData());
					n.getqReq().remove(); // req is done, removing
					RequestVehicle nextReq = (RequestVehicle) n.getqReq().peek(); // update next request if data sent
					if (nextReq != null) {
						if (end1 > nextReq.getStart()) {
							// System.out.println("update next req start at: " + end1);
							((RequestVehicle) n.getqReq().peek()).setStart(end1); // start after === end before
							((RequestVehicle) n.getqReq().peek()).setEnd(end1 + ((RequestVehicle) n.getqReq().peek()).getTimeProcess());
							check = true;
						} else if (end1 < nextReq.getStart() && nextReq.getStart() < ts) {
							check = true;
						}
					}
				}
			}
		}
	}

	private static void updateQRSU(List<NodeRSU> topoRSU, double ts) {
		// System.out.println("\nUPDATE QUEUE RSU");
		for (NodeRSU n : topoRSU) {
			boolean check = true;
			while (check && (n.getqReq().peek() != null)) {
				RequestRSU rv = (RequestRSU) n.getqReq().peek();
				double start1 = rv.getStart();
				double end1 = rv.getEnd();
				// System.out.print(n.getName() + " ");
				// System.out.println(rv.toString() + " " + start1 + " -> " + end1);
				check = false;
				if (end1 < ts) {
					n.getqReq().peek().setDone(true);
					n.getDoneReq().add(n.getqReq().peek()); // adding to done req
					// System.out.println("doneREQ: " + n.getqReq().peek().getStart() + " -> " + end1);
					n.getqReq().remove(); // req is done, removing
					RequestRSU nextReq = (RequestRSU) n.getqReq().peek(); // update next request
																			// if data sent
					if (nextReq != null) {
						if (end1 > nextReq.getStart()) {
							// System.out.println("update next req start at: " + end1);
							((RequestRSU) n.getqReq().peek()).setStart(end1);
							((RequestRSU) n.getqReq().peek())
									.setEnd(end1 + ((RequestRSU) n.getqReq().peek()).getTimeProcess());
							check = true;
						} else if (end1 < nextReq.getStart() && nextReq.getStart() < ts) {
							check = true;
						}
					}
				}
			}
		}
	}

	private static void insertQ(List<NodeVehicle> topo, List<RTable> rtable, int t) {
		// System.out.println("\nCALC assigned new workload and ADD new reqs to queue");
		// listCWL.write("a");
		Double key_mapREQ = (double) 0;
		for (NodeVehicle n : topo) {
			double aWL = 0; // all assigned workload as new-workload
			boolean check = false;
			for (RTable r : rtable) {
				double move_data = 0;
				if (r.getDes().equals(n.getName())) {
					aWL += r.getRatio() * r.getReq().getWL();
					double t_process = r.getRatio() * r.getReq().getWL() / r.getResource();

					// // calc moving data
					// if (r.getDes().equals(r.getReq().getSrcNode().getName())) { // source request node
					// 	move_data = aWL - Constants.RES[Constants.TYPE.VEHICLE.ordinal()];
					// 	if (move_data > 0) {
					// 		t_process = 1;
					// 		// listCWL.write("\n" + t + "\t" + n.getId() + "\t" + move_data);
					// 		aWL = Constants.RES[Constants.TYPE.VEHICLE.ordinal()];
					// 	} else {
					// 		move_data = 0;
					// 	}

					// }

					// adding queue
					double start = r.getTimeTrans() + (t - 1) * TS;
					NodeVehicle srcNode = r.getReq().getSrcNode();
					RequestBase rq = r.getReq();
					move_data = r.getReq().getWL();

					RequestVehicle rv = new RequestVehicle(rq.getId(), rq.getWL(), rq.getSrcNode(), rq.getTimeInit(),
											rq.isDone(), n.getId(), r.getRoute(), start, t_process, r.getRatio(), r.getTimeTrans(),
											start, (start + t_process + r.getTimeTrans()), r.getTimeSer(), move_data);

					n.getqReq().add(rv);

					// moving to nodeRSU
					if (r.getDes().equals(r.getReq().getSrcNode().getName())) {
						double dtVR = 0;
						// dt from V to R (fix 1hop and same BW)
						// need increase BW (V-R)
						dtVR = r.getRatio() * r.getReq().getWL() / Constants.BW; 
						RequestRSU rr = new RequestRSU(rv);
						rr.setTimeVR(dtVR);
						//System.out.println(rv.getTimeArrival());
						Vector<NodeRSU> vnr = n.getNodeParent().get(t);
						if (!vnr.isEmpty()) {
							NodeRSU nr = n.getNodeParent().get(t).get(0);
							nr.getqReqV().add(rr); // adding req to first parent
							// System.out.println(n.getName() + " move ->" + nr.getName() + " " + rr.toString());
						} else {
							// System.out.println("NO RSU TO TRANSFER DATA, NODE: " + n.getName());
						}
					}

					check = true;
				}
			}

			n.setaWL(n.getaWL() + aWL);
		}

	}
	
	private static void insertQ1(List<NodeVehicle> topo, List<RTable> rtable, int t) {
		// System.out.println("\nCALC assigned new workload and ADD new reqs to queue");
		// listCWL.write("a");
		for (NodeVehicle n : topo) {
			double aWL = 0; // all assigned workload as new-workload
			boolean check = false;
			for (RTable r : rtable) {
				double move_data = 0;
				if (r.getDes().equals(n.getName())) {
					aWL += r.getRatio() * r.getReq().getWL();
					double t_process = r.getRatio() * r.getReq().getWL() / r.getResource();

					// calc moving data
					if (r.getDes().equals(r.getReq().getSrcNode().getName())) { // source request node
						move_data = aWL - Constants.RES[Constants.TYPE.VEHICLE.ordinal()];
						if (move_data > 0) {
							t_process = 1;
							// listCWL.write("\n" + t + "\t" + n.getId() + "\t" + move_data);
							aWL = Constants.RES[Constants.TYPE.VEHICLE.ordinal()];
						} else {
							move_data = 0;
						}

					}

					// adding queue
					double start = r.getTimeTrans() + (t - 1) * TS; // the arrival task to Node, PSO at
																	// ts
					// double start = r.getTimeTrans()+(t)*TS;
					NodeVehicle srcNode = r.getReq().getSrcNode();
					RequestBase rq = r.getReq();

					RequestVehicle rv = new RequestVehicle(rq.getId(), rq.getWL(), rq.getSrcNode(), rq.getTimeInit(),
											rq.isDone(), n.getId(), r.getRoute(), start, t_process, r.getRatio(), r.getTimeTrans(),
											start, (start + t_process + r.getTimeTrans()), r.getTimeSer(), move_data);

					n.getqReq().add(rv);
					
					// if(move_data > 0)	System.out.println(rq.getSrcNode().getName() + " : " + move_data);

					// moving to nodeRSU
					if (r.getDes().equals(r.getReq().getSrcNode().getName())) {
						double dtVR = 0;
						// dt from V to R (fix 1hop and same BW)
						// need increase BW (V-R)
						dtVR = r.getRatio() * r.getReq().getWL() / Constants.BW; 
						RequestRSU rr = new RequestRSU(rv);
						rr.setTimeVR(dtVR);
						//System.out.println(rv.getTimeArrival());
						Vector<NodeRSU> vnr = n.getNodeParent().get(t);
						if (!vnr.isEmpty()) {
							NodeRSU nr = n.getNodeParent().get(t).get(0);
							nr.getqReqV().add(rr); // adding req to first parent
							// System.out.println(n.getName() + " move ->" + nr.getName() + " " + rr.toString());
						} else {
							// System.out.println("NO RSU TO TRANSFER DATA, NODE: " + n.getName());
						}
					}

					check = true;
				}
			}

			n.setaWL(n.getaWL() + aWL);
		}

	}

	private static void insertQRSU(List<NodeRSU> topoRSU, List<RTable> rtable, int t, List<NodeCloud> topoCloud) {
		for (NodeRSU n : topoRSU) {
			n.getqReqV().clear(); // remove all reqs from RSU
			double aWL = 0; // all assigned workload as new-workload
			boolean check = true;
			for (RTable r : rtable) {
				double move_data = 0;
				if (r.getDes().equals(n.getName())) {
					RequestRSU rq = (RequestRSU) r.getReq();
					if (r.getRatio() == 1) {
						aWL += r.getRatio() * r.getReq().getWL();
						double t_process = r.getRatio() * r.getReq().getWL() / r.getResource();
						// adding queue
						double start = r.getTimeTrans() + (t - 1) * TS;
						start += rq.getTimeVR();
						move_data = aWL - Constants.RES[Constants.TYPE.RSU.ordinal()];

						if (move_data > 0) {
							t_process = 1;
							aWL = Constants.RES[Constants.TYPE.RSU.ordinal()];
						} else {
							move_data = 0;
						}

						rq.setRatio(r.getRatio());
						rq.setStart(start);
						rq.setTimeArrival(start);
						rq.setTimeProcess(t_process);
						rq.setEnd(start + t_process);
						rq.setTimeTrans(r.getTimeTrans());
						rq.setTimeSer(r.getTimeSer());
						rq.setRoute(r.getRoute());
						rq.setMovedDataRSU(move_data);
						n.getqReq().add(rq);
						// moving RSU to Cloud
						// double dtRC = 0;
						// dt from V to R (fix 1hop and same BW)
						// need increase BW (V-R)
						// dtRC = move_data / Constants.BW; // time move Vehicle to RSU
						// RequestCloud rc = new RequestCloud(rq);
						// rc.setWlCloud(move_data);
						// rc.setTimeRC(dtRC);
						// rc.setRatio(1);
						// rc.setTimeVRC(dtRC + rq.getTimeVR() + r.getTimeTrans());
						// for (NodeCloud nr : topoCloud) {
						// 	nr.getqReqV().add(rc);
						// 	// System.out.println(n.getName() + " move ->" + nr.getName() + " " +
						// 	// rc.toString() + "\t"
						// 	// + rc.getWlCloud() + "\t" + rc.getTimeRC());
						// }
					}

				}
				check = false;
			}
			n.setaWL(n.getaWL() + aWL);
		}

		// n.getqReq().forEach(nqr->System.out.println(n.getName() + " : " +
		// nqr.toString()));
	}

	private static void calcTSerPSO(List<RTable> rtable, int testCase) {
		// 3.1 t_ser based PSO in rtable
		for (RTable r : rtable) {
			double compute = 0;
			double trans = 0;
			double workLoad = r.getReq().getWL();
			double subWL = r.getRatio() * workLoad; // new WL
			double totalWL = subWL + r.getcWL(); // adding cWL
			compute = totalWL / r.getResource(); // totalTime

			if (testCase != 2) {
				// calc t_process for all paths to node ~ including t_wait
				for (RTable r2 : rtable) {
					if (r2.getDes().equals(r.getDes())
							&& (r2.getId() != r.getId() || (r2.getReq().getId() != r.getReq().getId()))) {
						// adding route 2hop-2path
						compute += r2.getRatio() * r2.getReq().getWL() / r2.getResource();
						subWL += r2.getRatio() * r2.getReq().getWL(); // adding newWL route2
					}
				}
			}

			trans = (r.getRatio() * workLoad / Constants.BW1[0]) * r.getHop();

			if (r.getId() == 0) {
				trans = 0;
			}
			;

			r.setTimeCompute(compute);
			r.setTimeTrans(trans);
			double ser = compute + trans;
			r.setTimeSer(ser);

		} // END 3.1: LOG TIME IN RTABLE

	}

	private static void calcTSerPSORSU(List<RTable> rtable, int testCase) {
		// 3.1 t_ser based PSO in rtable
		for (RTable r : rtable) {
			double compute = 0;
			double trans = 0;
			double trans_rv = 0;
			double workLoad = r.getReq().getWL();
			double subWL = r.getRatio() * workLoad; // new WL
			double totalWL = subWL + r.getcWL(); // adding cWL
			compute = totalWL / r.getResource(); // totalTime

			if (testCase != 2) {
				// calc t_process for all paths to node ~ including t_wait
				for (RTable r2 : rtable) {
					if (r2.getDes().equals(r.getDes())
							&& (r2.getId() != r.getId() || (r2.getReq().getId() != r.getReq().getId()))) {
						// adding route 2hop-2path
						compute += r2.getRatio() * r2.getReq().getWL() / r2.getResource();
						subWL += r2.getRatio() * r2.getReq().getWL(); // adding newWL route2

					}
				}
			}

			trans = (r.getRatio() * workLoad / Constants.BW1[1]) * r.getHop(); // RSU to RSU

			if (r.getId() == 0) {
				trans = 0;
			}
			;

			RequestRSU rr = (RequestRSU) r.getReq();
			// trans += rr.getTimeVR(); // adding time trans VR

			r.setTimeCompute(compute);
			r.setTimeTrans(trans);

			double ser = compute + trans;
			r.setTimeSer(ser);

		} // END 3.1: LOG TIME IN RTABLE

	}

	private static void debug(String s, boolean mode) {
		if (mode)
			System.out.println(s);
	}

	private static double DecimalFormat1(double k) {
		double c = Math.round(k * 100.0) / 100.0;
		return c;
	}

	private static double Max(double a, double b, double c) {
		double max = a;
		if (max < b)
			max = b;
		if (max < c)
			max = c;
		return max;
	}

}