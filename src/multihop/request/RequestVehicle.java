package multihop.request;

import multihop.Constants;
import multihop.node.NodeVehicle;

public class RequestVehicle extends RequestBase {
	int des;
	String route;
	double timeArrival;
	double timeProcess;
	double ratio;
	double timeTrans;

	double start;
	double end;
	double timeSer;
	double movedData;
	double pri;
	NodeVehicle desNode;

	public RequestVehicle(int id, double wL, NodeVehicle srcNode, double timeInit, boolean done, int des, String route,
			double start, double end) {
		super(id, wL, srcNode, timeInit, done);
		this.des = des;
		this.route = route;
		this.start = start;
		this.end = end;
	}

	public RequestVehicle(int id, double wL, NodeVehicle srcNode, double timeInit, boolean done, int des, String route,
			double timeArrival, double timeProcess, double ratio, double timeTrans, double start, double end,
			double timeSer, double movedData, Double pri, NodeVehicle desNode) {
		super(id, wL, srcNode, timeInit, done);
		this.des = des;
		this.route = route;
		this.timeArrival = timeArrival;
		this.timeProcess = timeProcess;
		this.ratio = ratio;
		this.timeTrans = timeTrans;
		this.start = start;
		this.end = end;
		this.timeSer = timeSer;
		this.movedData = movedData;
		this.pri = pri;
		this.desNode = desNode;
	}

	public double getPri(){
		return pri;
	}

	public NodeVehicle getDesNode(){
		return desNode;
	}

	public int getDes() {
		return des;
	}

	public void setDes(int des) {
		this.des = des;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public double getTimeArrival() {
		return timeArrival;
	}

	public void setTimeArrival(double timeArrival) {
		this.timeArrival = timeArrival;
	}

	public double getTimeProcess() {
		return timeProcess;
	}

	public void setTimeProcess(double timeProcess) {
		this.timeProcess = timeProcess;
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public double getTimeTrans() {
		return timeTrans;
	}

	public void setTimeTrans(double timeTrans) {
		this.timeTrans = timeTrans;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double getEnd() {
		return end;
	}

	public void setEnd(double end) {
		this.end = end;
	}

	public double getTimeSer() {
		return timeSer;
	}

	public void setTimeSer(double timeSer) {
		this.timeSer = timeSer;
	}

	public double getMovedData() {
		return movedData;
	}

	public void setMovedData(double movedData) {
		this.movedData = movedData;
	}

	// @Override
	// public int compareTo(RequestBase o) {

	// 	// if ( Constants.threshold[0]*Constants.TS > ( ((RequestVehicle)o).getTimeArrival() - ((RequestVehicle)o).getTime() + Constants.TS ) && ((RequestVehicle)o).getPri() == true){
	// 	// 	return -1;
	// 	// }
	// 	// if(Constants.threshold[0]*Constants.TS > ( ((RequestVehicle)o).getTimeArrival() - ((RequestVehicle)o).getTime() + Constants.TS ) 
	// 	// 		&& ((RequestVehicle)o).getPri() == true
	// 	// 		&& ((RequestVehicle)o).getPri() == true 
	// 	// 		&& this.timeProcess > ((RequestVehicle)o).timeProcess ){
		
	// 	// 	return -1;
	// 	// }
	// 	// if(Constants.threshold[0]*Constants.TS > ( ((RequestVehicle)o).getTimeArrival() - ((RequestVehicle)o).getTime() + Constants.TS ) 
	// 	// 		&& ((RequestVehicle)o).getPri() == true
	// 	// 		&& ((RequestVehicle)o).getPri() == true 
	// 	// 		&& this.timeProcess < ((RequestVehicle)o).timeProcess ){
		
	// 	// 	return 1;
	// 	// }

	// 	// if ( Constants.threshold[1]*Constants.TS < ((RequestVehicle)o).getTimeProcess() && ((RequestVehicle)o).getPri() == false){
	// 	// 	return 1;
	// 	// }

	// 	// if (this.start >= ((RequestVehicle)o).start) {
	// 	// 	return 1;
	// 	// } else if (this.start < ((RequestVehicle)o).start) {
	// 	// 	return -1;
	// 	// }
	// 	// else {
	// 	// 	return 0;
	// 	// }

	// 	if (this.pri >= ((RequestVehicle)o).pri) {
	// 		return -1;
	// 	} else if (this.pri < ((RequestVehicle)o).pri) {
	// 		return 1;
	// 	}
	// 	else {
	// 		return 0;
	// 	}

	// }
}
