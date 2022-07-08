package multihop;

import multihop.node.NodeVehicle;
import multihop.request.RequestBase;
import multihop.request.RequestRSU;

public class RTable {
	int id;
	String des;
	String route;
	int hop;
	double ratio;
	double timeCompute;
	double timeTrans;
	double resource;
	int npath;
	double cWL;
	RequestBase req;
	double timeSer;
	int sameDes = 1;
	int sameReq = 0;
	// int decrease = 0;
	// NodeVehicle desNode = new NodeVehicle(50, "abc", 27, 27, 120, 30);
	NodeVehicle desNode;

	public NodeVehicle getDesNode() {
		return this.desNode;
	}

	public void setDesNode(NodeVehicle desNode) {
		this.desNode = desNode;
	}

	public int getSameReq() {
		return this.sameReq;
	}

	// public void setDecrease(int decrease) {
	// 	this.decrease = decrease;
	// }
	// public int getDecrease() {
	// 	return this.decrease;
	// }

	public void setSameReq(int sameReq) {
		this.sameReq = sameReq;
	}
	
	/**
	 * @param des is node destination
	 * @param route is next node 
	 * @param hop is number of hop from des
	 * @pram resource is capacity of node 
	 * */

	public RTable(int id, String des, String route, int hop, double resource ) {
		super();
		this.id = id;
		this.des = des;
		this.route = route;
		this.hop = hop;
		this.resource=resource;
		this.cWL=0;
	}
	
	
	public RTable(int id, String des, String route, int hop, double resource, RequestBase req) {
		super();
		this.id = id;
		this.des = des;
		this.route = route;
		this.hop = hop;
		this.resource=resource;
		this.cWL=0;
		this.req=req;
	}

	public RTable(int id, NodeVehicle des, String route, int hop, double resource, RequestBase req) {
		super();
		this.id = id;
		this.desNode = des;	this.des = this.desNode.getName();
		this.route = route;
		this.hop = hop;
		this.resource=resource;
		this.cWL=0;
		this.req=req;
	}

	public int getsameDes() {
		return this.sameDes;
	}

	public void setsameDes(int sameDes) {
		this.sameDes = sameDes;
	}

	public double getTimeSer() {
		return timeSer;
	}


	public void setTimeSer(double timeSer) {
		this.timeSer = timeSer;
	}


	public RequestBase getReq() {
		return req;
	}


	public void setReq(RequestBase req) {
		this.req = req;
	}


	public double getcWL() {
		return cWL;
	}


	public void setcWL(double cWL) {
		this.cWL = cWL;
	}


	public int getNpath() {
		return npath;
	}


	public void setNpath(int npath) {
		this.npath = npath;
	}


	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public String getRoute() {
		return route;
	}
	public void setRoute(String route) {
		this.route = route;
	}
	public int getHop() {
		return hop;
	}
	public void setHop(int hop) {
		this.hop = hop;
	}
	public double getRatio() {
		return ratio;
	}
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}
	public double getTimeCompute() {
		return timeCompute;
	}
	public void setTimeCompute(double timeCompute) {
		this.timeCompute = timeCompute;
	}
	public double getTimeTrans() {
		return timeTrans;
	}
	public void setTimeTrans(double timeTrans) {
		this.timeTrans = timeTrans;
	}
	
	public double getResource() {
		return resource;
	}
	public void setResource(double resource) {
		this.resource = resource;
	}
	public String toString() {
		String rtable = "id: " + id + " req:" + req.getId() + " src:" + req.getSrcNode().getName() + " des:" + des + " route:" + route + " p:" + ratio 
				+ " tComp:" + timeCompute + " tTrans:" + timeTrans + " tSer: " + (timeCompute+timeTrans)
				+ " hop:" + hop + " npath:" + npath 
				+ " cWL:" + cWL + " samedes:" + sameDes + " sameReq:" + sameReq + " decrease:" +this.desNode.getDecrease();
		
		return rtable;
	}
	
	public String toStringRSU() {
		String rtable = "id:" + id  
				+ " req" + req.getId() 
				+ " src:" + req.getSrcNode().getName()
				+ "-" + ((RequestRSU) req).getSrcNodeRSU().getName() 
				+ " des:" + des + " route:" + route + " p:" + ratio 
				+ " tComp:" + timeCompute + " tTrans:" + timeTrans + " tSer: " + (timeCompute+timeTrans)
				+ " hop:" + hop + " npath:" + npath 
				+ " cWL:" + cWL 
				+ " mWL:" + ((RequestRSU) req).getWlRSU(); 
				;
		
		return rtable;
	}
	
}
