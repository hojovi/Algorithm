/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.ht.huaweicodecraft2016;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public final class Route_dfs_dijkstra
{
    /**
     * 你需要完成功能的入口
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
    public static String searchRoute(String graphContent, String condition)
    {
        Route_dfs_dijkstra route=new Route_dfs_dijkstra();
        return route.goTwo(graphContent, condition);
    }
    
    private HashMap<Integer,HashMap<Integer,Integer>> diGraph=new HashMap<>();	//所有的点对应的权重,有向箭头的头,有向箭头的尾,权重
    private HashMap<Integer,HashSet<Integer>> revDiGraph=new HashMap<>();	//所有点对应的权重,有向箭头的尾,有向箭头的头
	private HashMap<Integer,ArrayList<Integer>> dict=new HashMap<>();		//所有的点对应的线段
	private HashMap<Integer,HashMap<Integer,Integer>> pointsRefLine=new HashMap<>();	//两点对应的线段的id
	private Integer endPoint;
	private Integer startPoint;
	private HashSet<Integer> points=new HashSet<>();	//V'中的点,不包括起点和终点
	private HashSet<Integer> allPoints=new HashSet<>();	//所有的点,不包括起点和终点
	
	private String goTwo(String graphContent,String condition){
		initialize(graphContent,condition);
		if(!trimAndSimpleJudge()){
			return "NA";
		}
		initializeDict();
		HashMap<Integer,Integer> path=GA(10);
		if(path==null){
			return "NA";
		}else{
			System.out.println("inconceivable! It has result");
			return "NA";
		}
	}
	
	private void initialize(String graphContent, String condition){
		//graphContent和Condition最后都有个回车
		graphContent=graphContent.substring(0, graphContent.length()-1);
		condition=condition.substring(0, condition.length()-1);
		
		//初始化v'=points, 起点=startPoint, 终点=endPoint
		String[] strs=condition.split(",");
		startPoint=Integer.valueOf(strs[0]);
		endPoint=Integer.valueOf(strs[1]);
		strs=strs[2].split("\\|");
		for(String s:strs){
			points.add(Integer.valueOf(s));
		}
		
		
		String lines[]=graphContent.split("\\n");
		for(String line:lines){
			strs=line.split(",");
			Integer lineID=Integer.valueOf(strs[0]);
			Integer srcID=Integer.valueOf(strs[1]);
			Integer desID=Integer.valueOf(strs[2]);
			Integer value = Integer.valueOf(strs[3]);
			
			if(srcID.equals(desID)){
				continue;
			}
			
			//记录所有的点
			if(!allPoints.contains(srcID)){
				allPoints.add(srcID);
			}
			if(!allPoints.contains(desID)){
				allPoints.add(desID);
			}
			
			//将数据插入diGraph和pointsRefLine
			if (!diGraph.containsKey(srcID)) {
				diGraph.put(srcID, new HashMap<Integer, Integer>());
				pointsRefLine.put(srcID, new HashMap<Integer,Integer>());
			}
			HashMap<Integer, Integer> map = diGraph.get(srcID);
			HashMap<Integer, Integer> ref = pointsRefLine.get(srcID);
			if (map.containsKey(desID)) {
				Integer preValue = map.get(desID);
				if (value < preValue) {
					map.put(desID, value);
					ref.put(desID, lineID);
				}
			} else {
				map.put(desID, value);
				ref.put(desID, lineID);
			}
			
			//初始化revDiGraph,后加上的
			if(!revDiGraph.containsKey(desID)){
				revDiGraph.put(desID, new HashSet<Integer>());
			}
			HashSet<Integer> mapRev=revDiGraph.get(desID);
			if(!mapRev.contains(srcID)){
				mapRev.add(srcID);
			}
		}
		
		//删除allPoints的startPoint和endPoint
		allPoints.remove(startPoint);
		allPoints.remove(endPoint);
	}
	
	//试图递归删除除终点外出度为0的点,并做简单的判断,如果v'中有点出度为0,那肯定没有结果
	//TODO 还可以接着进行删除,找出跟起点和终点连接的所有点,然后把其他点删除掉
	public boolean trimAndSimpleJudge(){
		//下面的本来是要消除掉图中那些根本与startPoint和endPoint不连接的点,但我又想了想,用不着,就算连肯定也连不到
//		HashSet<Integer> set=new HashSet<>(allPoints);
//		set.add(startPoint);
//		set.add(endPoint);
//		ArrayDeque<Integer> deq=new ArrayDeque<>();
//		deq.add(startPoint);
//		while(set.size()>0&&deq.size()>0){
//			final int size=deq.size();
//			for (int i = 0; i < size; i++) {
//				Integer cur = deq.poll();
//				set.remove(cur);
//				HashMap<Integer, Integer> map = diGraph.get(cur);
//				Iterator<Integer> it = map.keySet().iterator();
//				while (it.hasNext()) {
//					Integer nextPoint = it.next();
//					if (set.contains(nextPoint)) {
//						deq.add(nextPoint);
//					}
//				}
//			}
//		}
//		if(!set.isEmpty()){
//			Iterator<Integer> it=set.iterator();
//			while(it.hasNext()){
//				Integer point=it.next();
//				HashMap<Integer,Integer> map=diGraph.get(point);
//				HashSet<Integer> revMap=revDiGraph.get(point);
//				Iterator<Integer> itMap=map.keySet().iterator();
//				while(itMap.hasNext()){
//					revDiGraph.get(itMap.next()).remove(point);
//				}
//				Iterator<Integer> itRevMap=revMap.iterator();
//				while(itRevMap.hasNext()){
//					diGraph.get(itRevMap.next()).remove(point);
//				}
//				diGraph.remove(point);
//				revDiGraph.remove(point);
//			}
//		}
		while(allPoints.size()>0){
			Iterator<Integer> it=allPoints.iterator();
			Integer index=-1;		//应该删除的点
			while(it.hasNext()){
				Integer cur=it.next();
				if(!diGraph.containsKey(cur)&&!cur.equals(endPoint)){
					index=cur;
					break;
				}
			}
			//如果找到的是终点,那没什么意义
			if(index.equals(endPoint)){
				continue;
			}
			//如果找到一个在v'中的点,那就扯犊子了
			if(points.contains(index)){
				return false;
			}
			if(index==-1){
				//已经没有出度为0的点了
				return true;
			}else{
				//开始删除
				HashSet<Integer> revMap=revDiGraph.get(index);
				Iterator<Integer> itt=revMap.iterator();
				while(itt.hasNext()){
					Integer srcID=itt.next();
					diGraph.get(srcID).remove(index);
				}
				revDiGraph.remove(index);
				allPoints.remove(index);
			}
		}
		//程序运行到这里就更扯淡了,怎么没点了?
		return false;
	}
	
	private void initializeDict(){
		Iterator<Entry<Integer,HashMap<Integer,Integer>>> it=diGraph.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer,HashMap<Integer,Integer>> entry=it.next();
			Integer key=entry.getKey();
			HashMap<Integer,Integer> value=entry.getValue();
			ArrayList<Integer> list=new ArrayList<>();
			Iterator<Entry<Integer,Integer>> itt=value.entrySet().iterator();
			while(itt.hasNext()){
				list.add(itt.next().getKey());
			}
			dict.put(key, list);
		}
	}
	
	private HashMap<Integer,Integer> GA(int generations){
		ArrayList<HashMap<Integer,Integer>> population=initialize();
		for(int i=0;i<generations;i++){
			produceNextGeneration(population);
		}
		HashMap<Integer,Integer> path=population.get(0);
		if(isAvaliablePath(path)){
			return path;
		}else{
			return null;
		}
	}
	
	private boolean isAvaliablePath(HashMap<Integer,Integer> path){
		Integer curPoint=startPoint;
		HashSet<Integer> set=(HashSet<Integer>) points.clone();
		while(curPoint!=endPoint){
			if(set.contains(curPoint)){
				set.remove(curPoint);
			}
			if(diGraph.get(curPoint).containsKey(path.get(curPoint))){
				return false;
			}
			curPoint=path.get(curPoint);
		}
		return set.isEmpty();
	}
	
	//初始化,随机生成一系列的解
	private ArrayList<HashMap<Integer,Integer>> initialize(){
		ArrayList<HashMap<Integer,Integer>> deq=new ArrayList<>();
		for(int i=0;i<allPoints.size();i++){
			deq.add(shuffle(allPoints));
		}
		return deq;
	}
	
	private void produceNextGeneration(ArrayList<HashMap<Integer,Integer>> population){
		ArrayList<HashMap<Integer,Integer>> children=new ArrayList<>();
		int[] randomSequence=shuffle(population.size());
		Random random=new Random();
		for(int i=1;i<randomSequence.length;i+=2){
			children.add(crossAndVary(population.get(randomSequence[i]),population.get(randomSequence[i-1]),random,0.1,0.1));
		}
		weed(population,children);
	}
	
	//交叉并变异
	private HashMap<Integer,Integer> crossAndVary(HashMap<Integer,Integer> p1,HashMap<Integer,Integer> p2,Random random,double crossRate,double varyRate){
		HashMap<Integer,Integer> child=new HashMap<Integer,Integer>();
		//交叉概率
		
		//变异概率(指一个点突然变成另一个点)
		
//貌似用不着写缺失概率或者是增加概率
//		//缺失概率(指当前的点突然消失)
//		double deficientRate=0.1;
//		//增加概率(指的是当前位置突然增加了一个点)
//		double accrescence=0.1;
		
		HashMap<Integer,Integer> tmp=p1;
		Integer curPoint=startPoint;
		while(true){
			if(curPoint==endPoint){
				break;
			}
			Integer nextPoint=tmp.get(curPoint);
			if(child.containsKey(nextPoint)){
				if(tmp==p1){
					tmp=p2;
				}else{
					tmp=p1;
				}
				nextPoint=tmp.get(curPoint);
				if(child.containsKey(nextPoint)){
					ArrayList<Integer> list=dict.get(curPoint);
					nextPoint=list.get(random.nextInt(list.size()));
					if(child.containsKey(nextPoint)){
						for(Integer point:list){
							if(!child.containsKey(point)){
								nextPoint=point;
							}
						}
					}
				}
			}
			if(child.containsKey(nextPoint)){
				
			}
			//看看是否变异
			if(random.nextDouble()<0.1){
				ArrayList<Integer> list=dict.get(curPoint);
				nextPoint=list.get(random.nextInt(list.size()));
				if(!child.containsKey(nextPoint)){
					child.put(curPoint, nextPoint);
					curPoint=nextPoint;
					continue;
				}
			}
			//看看是否交叉
			if(child.containsKey(tmp.get(curPoint))||random.nextDouble()<0.1){
				if(tmp==p1){
					tmp=p2;
				}else{
					tmp=p1;
				}
			}
			if(child.containsKey(tmp.get(curPoint))){
				curPoint=tmp.get(curPoint);
				continue;
			}
			child.put(curPoint, tmp.get(curPoint));
			curPoint=child.get(curPoint);
		}
		return child;
	}
	
	//评判某段路径的好坏
	private double judge(HashMap<Integer,Integer> path){
		int pointsCount=0;	//路径中包含v'中点的条数
		int weight=0;		//路径中所有存在路径的权重和
		int noExistCount=1;	//路径中不存在的路径的条数, 初始值为1是为了防止出现除数为0
		Integer tmp=startPoint;
		while(tmp!=endPoint){
			if(points.contains(tmp)){
				pointsCount++;
			}
			if(diGraph.get(tmp).containsKey(path.get(tmp))){
				weight+=diGraph.get(tmp).get(path.get(tmp));
			}else{
				noExistCount+=1;
			}
			tmp=path.get(tmp);
		}
		//如果weight为0怎么办
		//这里的做法是如果weight为0直接返回0
		if(weight==0){
			return 0.0;
		}
		
		//路径中包含v'中点的条数越大越好,权重和越小越好,不存在的路径越少越好
		return ((double)pointsCount)/(weight*noExistCount);
	}
	
	//进行淘汰
	private void weed(ArrayList<HashMap<Integer, Integer>> parents,ArrayList<HashMap<Integer,Integer>> children){
		HashMap<HashMap<Integer,Integer>,Double> map=new HashMap<>();
		for(HashMap<Integer,Integer> path:parents){
			map.put(path, judge(path));
		}
		final HashMap<HashMap<Integer,Integer>,Double> parentJudge=map;
		Collections.sort(parents,new Comparator<HashMap<Integer,Integer>>(){

			@Override
			public int compare(HashMap<Integer, Integer> arg0, HashMap<Integer, Integer> arg1) {
				double j0=parentJudge.get(arg0);
				double j1=parentJudge.get(arg1);
				if(j0>j1){
					return -1;
				}else if(j0==j1){
					return 0;
				}else{
					return 1;
				}
			}
			
		});
		HashMap<HashMap<Integer,Integer>,Double> mapp=new HashMap<>();
		for(HashMap<Integer,Integer> path:children){
			mapp.put(path, judge(path));
		}
		final HashMap<HashMap<Integer,Integer>,Double> childJudge=mapp;
		Collections.sort(children,new Comparator<HashMap<Integer,Integer>>(){

			@Override
			public int compare(HashMap<Integer, Integer> arg0, HashMap<Integer, Integer> arg1) {
				double j0=childJudge.get(arg0);
				double j1=childJudge.get(arg1);
				if(j0>j1){
					return -1;
				}else if(j0==j1){
					return 0;
				}else{
					return 1;
				}
			}
			
		});
		int i=0,j=0;
		int count=0;
		while(i<parents.size()&&j<children.size()){
			if(count==parents.size()){
				break;
			}
			if(parentJudge.get(parents.get(i))>=childJudge.get(children.get(j))){
				++i;
			}else{
				++j;
			}
			++count;
		}
		j=0;
		while(i<parents.size()){
			parents.set(i, children.get(j));
			++i;
			++j;
		}
	}
	
	//洗牌, 给遗传算法提供初始值
	private HashMap<Integer,Integer> shuffle(HashSet<Integer> points){
		ArrayList<Integer> list=new ArrayList<>();
		list.addAll(points);
		Random random=new Random();
		for(int i=0;i<list.size();i++){
			int j=random.nextInt(list.size());
			Integer tmp=list.get(j);
			list.set(j, list.get(i));
			list.set(i, tmp);
		}
		HashMap<Integer,Integer> result=new HashMap<>();
		for(int i=0;i<list.size()-1;i++){
			result.put(list.get(i), list.get(i+1));
		}
		result.put(startPoint, list.get(0));
		result.put(list.get(list.size()-1), endPoint);
		return result;
	}
	
	//产生一个0到n之间的随机序列,不包括n
	private int[] shuffle(int n){
		int[] result=new int[n];
		Random random=new Random();
		for(int i=0;i<n;i++){
			result[i]=i;
		}
		for(int i=0;i<n;i++){
			int j=random.nextInt(n);
			int tmp=result[j];
			result[j]=result[i];
			result[i]=tmp;
		}
		return result;
	}
	
	//事实证明,在解NP难问题的时候, 遗传算法,模拟退火算法 等看似不靠谱的算法可能取到很好的效果,原因很简单,毕竟它比暴力枚举强啊
	//先用Dijkstra算法找出最短路径,先不关心是否包括v'中的点
	//用哈希表,每个key的value代表它的前一个点,用values哈希表表示从startPoint到key的权重
	//求出最短路径暂时还看不出来有什么用
	private HashMap<Integer,Integer> dijkstra(){
		ArrayDeque<Integer> deq=new ArrayDeque<>();
		deq.add(startPoint);
		HashSet<Integer> visited=new HashSet<>();
		visited.add(startPoint);
		HashMap<Integer,Integer> map=new HashMap<>();
		HashMap<Integer,Integer> values=new HashMap<>();
		values.put(startPoint, 0);
		
		while(deq.size()>0){
			int size=deq.size();
			HashSet<Integer> curVisited=new HashSet<>();
			while(size>0){
				Integer curPoint=deq.poll();
				int curValue=values.get(curPoint);
				HashMap<Integer,Integer> nextPoints=diGraph.get(curPoint);
				Iterator<Entry<Integer,Integer>> it=nextPoints.entrySet().iterator();
				while(it.hasNext()){
					Entry<Integer,Integer> entry=it.next();
					Integer nextPoint=entry.getKey();
					if(visited.contains(nextPoint)){
						continue;
					}
					int nextValue=entry.getValue()+curValue;
					if(values.containsKey(nextPoint)&&values.get(nextPoint)<nextValue){
						//do nothing
					}else{
						map.put(nextPoint, curPoint);
						values.put(nextPoint, nextValue);
					}
					curVisited.add(nextPoint);
					deq.add(nextPoint);
				}
				--size;
			}
			visited.addAll(curVisited);
		}
		return map;
	}
}