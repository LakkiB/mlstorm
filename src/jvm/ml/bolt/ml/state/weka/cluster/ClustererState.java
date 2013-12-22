package bolt.ml.state.weka.cluster;

import bolt.ml.state.weka.BaseOnlineState;
import bolt.ml.state.weka.utils.WekaUtils;
import weka.clusterers.Cobweb;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Collection;

/**
 * User: lbhat <laksh85@gmail.com>
 * Date: 12/16/13
 * Time: 7:45 PM
 */

/**
 * Example of a clustering state
 * <p/>
 * Look at abstract base class for method details
 * The base class gives the structure and the ClassifierState classes implement them
 */

public class ClustererState extends BaseOnlineState {
    private Cobweb clusterer;
    private int numClusters;


    public ClustererState(int numClusters, int windowSize) {
        super(windowSize);
        // This is where you create your own classifier and set the necessary parameters
        clusterer = new Cobweb();
        this.numClusters = numClusters;
    }

    @Override
    public void train(Instances trainingInstances) throws Exception {
        while (trainingInstances.enumerateInstances().hasMoreElements()) {
            train((Instance) trainingInstances.enumerateInstances().nextElement());
        }
    }

    @Override
    protected void postUpdate() {
        this.clusterer.updateFinished();
    }

    @Override
    protected synchronized void preUpdate() throws Exception {
        Collection<double[]> features = featureVectorsInWindow.values();
        for (double[] some : features) {
            loadWekaAttributes(some);
            break;
        }
        Instances data = new Instances("training", this.wekaAttributes, 0);
        clusterer.buildClusterer(data.stringFreeStructure());
    }

    @Override
    public int predict(Instance testInstance) throws Exception {
        assert (testInstance != null);
        return clusterer.clusterInstance(testInstance);
    }

    @Override
    protected synchronized void loadWekaAttributes(final double[] features) {
        if (this.wekaAttributes == null) {
            this.wekaAttributes = WekaUtils.getFeatureVectorForOnlineClustering(numClusters, features.length);
            this.wekaAttributes.trimToSize();
        }
    }

    @Override
    protected void train(Instance instance) throws Exception {
        if(instance != null) clusterer.updateClusterer(instance);
    }

    public int getNumClusters() {
        return numClusters;
    }
}
