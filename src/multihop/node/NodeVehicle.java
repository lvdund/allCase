package multihop.node;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import multihop.Constants;
import multihop.request.RequestBase;
import multihop.request.RequestVehicle;

public class NodeVehicle extends NodeBase {
	double[] x;
	double[] y;
	double[] velo;
	double[] phi;
	double[] sign;
	List<Integer> decrease = new ArrayList<>();
	boolean pri;

	double cWL = 0; // current workload
	double aWL = 0; // assigned workload

	Vector<Vector<NodeVehicle>> nodeNeighbor = new Vector<Vector<NodeVehicle>>();
	Vector<Vector<NodeRSU>> nodeParent = new Vector<Vector<NodeRSU>>();

	Queue<RequestVehicle> qReq = new PriorityQueue<RequestVehicle>();

	List<RequestVehicle> doneReq = new ArrayList<RequestVehicle>();


	public NodeVehicle(int id, String name, double lat, double lng, int range, double res) {
		super(id, name, lat, lng, range, res);
		// TODO Auto-generated constructor stub
		this.decrease.add(0);
		this.pri = false;
	}

	public Boolean checkLK(NodeVehicle a, int i) {
		double ax = a.getX()[i];
		double ay = a.getY()[i];
		double k = (this.x[i] - ax) * (this.x[i] - ax) + (this.y[i] - ay) * (this.y[i] - ay);
		if (k <= RANGE) {
			return true;
		}
		return false;
	}
	
	public boolean checkLK(NodeVehicle a, int i, NodeRSU b) {
		double ax = a.getX()[i];
		double ay = a.getY()[i];
		double bx = b.getLat();
		double by = b.getLng();
		double k = (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
		if (k <= RANGE) {
			return true;
		}
		return false;
	}

	public String toString() {
		
		return name + ": " + lat + " , " + lng;
		
	}
	
	public List<Integer> getDecrease() {
		return this.decrease;
	}

	// public void setDecrease(List<Integer> decrease) {
	// 	this.decrease = decrease;
	// }

	public void setPri(boolean pri){
		this.pri = pri;
	}

	public boolean getPri(){
		return this.pri;
	}

	public void setDecreaseGain(){
		this.decrease.add(this.decrease.get(this.decrease.size()-1)+1);
	}

	public void setDecreaseZero(){
		this.decrease.add(0);
	}

	public Vector<Vector<NodeRSU>> getNodeParent() {
		return nodeParent;
	}

	public void setNodeParent(Vector<Vector<NodeRSU>> nodeParent) {
		this.nodeParent = nodeParent;
	}

	public double getaWL() {
		return aWL;
	}

	public void setaWL(double aWL) {
		this.aWL = aWL;
	}

	public double getcWL() {
		return cWL;
	}

	public void setcWL(double cWL) {
		this.cWL = cWL;
	}

	public Queue<RequestVehicle> getqReq() {
		return qReq;
	}

	public void setqReq(Queue<RequestVehicle> qReq) {
		this.qReq = qReq;
	}

	public List<RequestVehicle> getDoneReq() {
		return doneReq;
	}

	public void setDoneReq(List<RequestVehicle> doneReq) {
		this.doneReq = doneReq;
	}

	public Vector<Vector<NodeVehicle>> getNodeNeighbor() {
		return nodeNeighbor;
	}

	public void setNodeNeighbor(Vector<Vector<NodeVehicle>> nodeNeighbor) {
		this.nodeNeighbor = nodeNeighbor;
	}

	public Boolean checkLK(NodeVehicle a) {
		return null;
	}

	public double[] getX() {
		return x;
	}

	public void setX(double[] x) {
		this.x = x;
	}

	public double[] getY() {
		return y;
	}

	public void setY(double[] y) {
		this.y = y;
	}

	public double[] getVelo() {
		return velo;
	}

	public void setVelo(double[] velo) {
		this.velo = velo;
	}

	public double[] getPhi() {
		return phi;
	}

	public void setPhi(double[] phi) {
		this.phi = phi;
	}

	public double[] getSign() {
		return sign;
	}

	public void setSign(double[] sign) {
		this.sign = sign;
	}


}
