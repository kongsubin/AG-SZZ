package hgu.csee.isel.alinew.szz.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import hgu.csee.isel.alinew.szz.model.RevsWithPath;

public class AnnotationGraphBuilder {

	public AnnotationGraphModel buildAnnotationGraph(Repository repo, RevsWithPath revsWithPath, boolean debug) {
		// Phase 1 : split Map (i.e. RevsWithPath)
		int mapSize = revsWithPath.size();
		int arrSize = 10;

		// when map has less than 10 elements.
		if (mapSize < arrSize)
			arrSize = mapSize;

		RevsWithPath[] revsWithPathArr = new RevsWithPath[arrSize];
		for (int i = 0; i < arrSize; i++) {
			revsWithPathArr[i] = new RevsWithPath();
		}

		int count = 0;

		for (Map.Entry<String, List<RevCommit>> elem : revsWithPath.entrySet()) {
			revsWithPathArr[count % arrSize].put(elem.getKey(), elem.getValue());
			count++;
		}

		// Phase 2 : Execute thread pool
		ArrayList<AnnotationGraphBuilderThread> AGRunners = new ArrayList<AnnotationGraphBuilderThread>();

		int numOfCoresInMyCPU = Runtime.getRuntime().availableProcessors();

		ExecutorService executor = Executors.newFixedThreadPool(numOfCoresInMyCPU);

		for (int i = 0; i < arrSize; i++) {
			Runnable worker = new AnnotationGraphBuilderThread(repo, revsWithPathArr[i], debug);
			executor.execute(worker);
			AGRunners.add((AnnotationGraphBuilderThread) worker);
		}

		executor.shutdown(); // no new tasks will be accepted.

		while (!executor.isTerminated()) {
			
		}

		AnnotationGraphModel annotationGraph = new AnnotationGraphModel();

		for (AnnotationGraphBuilderThread runner : AGRunners) {
			annotationGraph.putAll(runner.partitionedAnnotationGraph);
		}

		return annotationGraph;
	}

}