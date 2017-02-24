package weihua.myassistant.util;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;

public class AmapUtil {

	private static Logger loger = Logger.getLogger(AmapUtil.class);

	private static final String key = "4f7b678a63bebec310250c56109aabb8";

	public static void main(String[] args) throws Exception {
		CircleParam param = new CircleParam();
		param.location = "116.572416,39.768533";
		param.radius = "500";
		Trafficstatus trafficstatus = AmapUtil.<Trafficstatus, CircleParam> getTrafficInfo(param);
		loger.info(trafficstatus);

		RectangleParam param1 = new RectangleParam();
		param1.lowerLeftCorner = "116.546519,39.76229";
		param1.upperRightCorner = "116.572416,39.768533";
		Trafficstatus trafficstatus1 = AmapUtil.<Trafficstatus, RectangleParam> getTrafficInfo(param1);
		loger.info(trafficstatus1);

		WalkParam param2 = new WalkParam();
		param2.origin = "116.546519,39.76229";
		param2.destination = "116.572416,39.768533";
		TrafficDirection trafficDirection2 = AmapUtil.<TrafficDirection, WalkParam> getTrafficInfo(param2);
		loger.info(trafficDirection2);

		DrivingParam param3 = new DrivingParam();
		param3.origin = "116.545811,39.761333";
		param3.destination = "116.565724,39.789937";
		param3.strategy = "2";
		param3.waypoints = "116.572236,39.768194";
		TrafficDirection trafficDirection3 = AmapUtil.<TrafficDirection, DrivingParam> getTrafficInfo(param3);
		loger.info(trafficDirection3);

		IntegratedParam param4 = new IntegratedParam();
		param4.origin = "116.546101,39.755967";
		param4.destination = "116.448545,39.834228";
		param4.city = "北京";
		param4.date = DateUtil.getCurrentDateString();
		param4.time = DateUtil.getCurrentTimeString();
		TrafficTransits trafficTransits = AmapUtil.<TrafficTransits, IntegratedParam> getTrafficInfo(param4);
		loger.info(trafficTransits);
	}

	public static <R extends Result, P extends Param> R getTrafficInfo(P param) throws Exception {
		R trafficResult = null;
		String apiUrl = null;
		String json = null;
		loger.info("TrafficInfo param:" + param);
		try {
			if (param instanceof WalkParam) {
				apiUrl = getApiWalkUrl((WalkParam) param);
				json = HttpUtil.get(apiUrl, null, null);
				trafficResult = GsonUtil.getEntityFromJson(json, new TypeToken<TrafficDirection>() {
				});
			} else if (param instanceof DrivingParam) {
				apiUrl = getApiDrivingUrl((DrivingParam) param);
				json = HttpUtil.get(apiUrl, null, null);
				trafficResult = GsonUtil.getEntityFromJson(json, new TypeToken<TrafficDirection>() {
				});
			} else if (param instanceof IntegratedParam) {
				apiUrl = getApiIntegratedUrl((IntegratedParam) param);
				json = HttpUtil.get(apiUrl, null, null);
				trafficResult = GsonUtil.getEntityFromJson(json, new TypeToken<TrafficTransits>() {
				});
			} else if (param instanceof CircleParam) {
				apiUrl = getApiCircleUrl((CircleParam) param);
				json = HttpUtil.get(apiUrl, null, null);
				trafficResult = GsonUtil.getEntityFromJson(json, new TypeToken<Trafficstatus>() {
				});
			} else if (param instanceof RectangleParam) {
				apiUrl = getApiRectangleUrl((RectangleParam) param);
				json = HttpUtil.get(apiUrl, null, null);
				trafficResult = GsonUtil.getEntityFromJson(json, new TypeToken<Trafficstatus>() {
				});
			}
		} catch (Exception e) {
			throw new Exception(ExceptionUtil.getStackTrace(e) + "\n" + "Json data:" + json);
		}

		return trafficResult;

	}

	/* 规划查询begin */

	private static String getApiWalkUrl(WalkParam param) {
		return "http://restapi.amap.com/v3/direction/walking?key=" + key + "&origin=" + param.origin + "&destination="
				+ param.destination;
	}

	private static String getApiIntegratedUrl(IntegratedParam param) {
		return "http://restapi.amap.com/v3/direction/transit/integrated?key=" + key + "&origin=" + param.origin
				+ "&destination=" + param.destination + "&city=" + param.city + "&strategy=" + param.strategy
				+ "&nightflag=" + param.nightflag + "&date=" + param.date + "&time=" + param.time + "&cityd="
				+ param.cityd;
	}

	private static String getApiDrivingUrl(DrivingParam param) {
		return "http://restapi.amap.com/v3/direction/driving?key=" + key + "&origin=" + param.origin + "&destination="
				+ param.destination + "&extensions=" + param.extensions + "&strategy=" + param.strategy + "&waypoints="
				+ param.waypoints + "&avoidpolygons=" + param.avoidpolygons;
	}

	public static class WalkParam implements Param {
		public String origin;
		public String destination;

		@Override
		public String toString() {
			return "[origin:" + origin + ",destination:" + destination + "]";
		}
	}

	public static class IntegratedParam implements Param {
		public String origin;
		public String destination;
		public String city;
		public String cityd = "";
		public String strategy = "0";
		public String nightflag = "0";
		public String date;
		public String time;

		@Override
		public String toString() {
			return "[origin:" + origin + ",destination:" + destination + ",city:" + city + ",cityd:" + cityd
					+ ",strategy:" + strategy + ",nightflag:" + nightflag + ",date:" + date + ",time:" + time + "]";
		}
	}

	public static class DrivingParam implements Param {
		public String origin;
		public String destination;
		public String extensions = "base";
		public String strategy = "0";
		public String waypoints;
		public String avoidpolygons;

		@Override
		public String toString() {
			return "[origin:" + origin + ",destination:" + destination + ",extensions:" + extensions + ",strategy:"
					+ strategy + ",waypoints:" + waypoints + ",avoidpolygons:" + avoidpolygons + "]";
		}
	}

	public static class TrafficTransits implements Result {
		public String status;
		public String info;
		public String infocode;
		public String count;
		public Route route;

		public static class Route {
			public String origin;
			public String destination;
			public String distance;
			public String taxi_cost;
			public List<Transit> transits;

			public static class Transit {
				public String cost;
				public String duration;
				public String nightflag;
				public String walking_distance;
				public String distance;
				public String missed;
				public List<Segment> segments;

				public static class Segment {
					public Walking walking;
					public Bus bus;
					public String nightflag;
					public String walking_distance;

					public static class Walking {
						public String origin;
						public String destination;
						public String distance;
						public String duration;
						public List<Step> steps;

						public static class Step {
							public String instruction;
							public String distance;
							public String polyline;
						}
					}

					public static class Bus {
						public List<Busline> buslines;

						public static class Busline {
							public String id;
							public String name;
							public Object type;
							public String duration;
							public String distance;
							public String polyline;
							public String via_num;
							public Station departure_stop;
							public Station arrival_stop;
							public Object start_time;
							public Object end_time;
							public List<Station> via_stops;

							public static class Station {
								public String id;
								public String name;
								public String location;
							}
						}
					}
				}
			}
		}
	}

	public static class TrafficDirection implements Result {
		public String status;
		public String info;
		public String infocode;
		public Route route;

		public static class Route {
			public String origin;
			public String destination;
			public List<Path> paths;

			public static class Path {
				public String distance;
				public String duration;
				public List<Step> steps;

				public static class Step {
					public String instruction;
					public String distance;
					public String polyline;
				}
			}
		}
	}

	/* 规划查询end */

	/* 路况查询begin */

	private static String getApiRectangleUrl(RectangleParam param) {
		return "http://restapi.amap.com/v3/traffic/status/rectangle?key=" + key + "&rectangle=" + param.lowerLeftCorner
				+ ";" + param.upperRightCorner;
	}

	private static String getApiCircleUrl(CircleParam param) {
		return "http://restapi.amap.com/v3/traffic/status/circle?key=" + key + "&location=" + param.location
				+ "&radius=" + param.radius;
	}

	public static class RectangleParam implements Param {
		public String lowerLeftCorner;
		public String upperRightCorner;

		@Override
		public String toString() {
			return "[lowerLeftCorner:" + lowerLeftCorner + ",upperRightCorner:" + upperRightCorner + "]";
		}
	}

	public static class CircleParam implements Param {
		public String location;
		public String radius;

		@Override
		public String toString() {
			return "[location:" + location + ",radius:" + radius + "]";
		}
	}

	public static class Trafficstatus implements Result {
		public String status;
		public String info;
		public String infocode;
		public Trafficinfo trafficinfo;

		public static class Trafficinfo {
			public String description;
			public Evaluation evaluation;

			public static class Evaluation {
				public String expedite;
				public String congested;
				public String blocked;
				public String unknown;
				public String status;
				public String description;
			}
		}
	}

	/* 路况查询end */

	public interface Result {

	}

	public interface Param {

	}
}