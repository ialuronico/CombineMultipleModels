/*
 *    CombineMultipleModels.java
 *
 */

package weka.classifiers.meta;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import java.util.Arrays;

/**
<!-- globalinfo-start -->
* It combines the results from meta classifiers to build only one model.
* <p/>
<!-- globalinfo-end -->
*
<!-- options-start -->
* Valid options are: <p/>
* 
* <pre> -PE
*  Size of generated training set to build the combined model. 
*  Input format is the percentage of the training set size. 
*  (default as many as the supplied training set, 100%)</pre>
* 
* <pre> -Z
*  Meta learner that produces the models to combine. 
*  (default: weka.classifiers.meta.Bagging)</pre>
*  
* <pre> -S &lt;num&gt;
*  Random number seed.
*  (default 1)</pre>
* 
* <pre> -D
*  If set, classifier is run in debug mode and
*  may output additional info to the console</pre>
* 
* <pre> -W
*  Full name of base classifier.
*  (default: weka.classifiers.trees.J48)</pre>
* 
<!-- options-end -->
*
* Options after -- are passed to the designated classifier.<p>
*
* @author Simone Romano
*/
public class CombineMultipleModels extends RandomizableSingleClassifierEnhancer{
	/** Size of generated training set to build the combined model. 
	 *  Percentage of training set size. */
	protected int PE = 100;
	/** Meta learner that produces the models to combine.*/
	protected SingleClassifierEnhancer m_MetaClassifier;
	/** Get method for N <br>
	 * Size of generated training set to build the combined model */
	public int getPE(){
		return PE;
	}
	/** Set method for N <br>
	 * Size of generated training set to build the combined model */
	public void setPE(int PE){
		this.PE = PE;
	}
	/** Get method for m_MetaClassifier <br>
	 * Meta learner that produces the models to combine */
	public SingleClassifierEnhancer getMetaClassifier(){
		return m_MetaClassifier;
	}
	/** Set method for m_MetaClassifier <br>
	 * Meta learner that produces the models to combine */
	public void setMetaClassifier(SingleClassifierEnhancer metaClassifier){
		m_MetaClassifier = metaClassifier;
	}
  /**
   * Constructor.
   */
  public CombineMultipleModels() {
    m_Classifier = new weka.classifiers.trees.J48();
    m_MetaClassifier = new weka.classifiers.meta.Bagging();
  }
  /**
   * Returns a string describing classifier
   * @return a description suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
     return "It combines the results from meta classifiers to build only one model.";
  }
  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
  	Vector newVector = new Vector(2);
  	newVector.addElement(new Option("\tSize of generated training set to build the combined model. " +
  																	"Input format is the percentage of the training set size. " +
  																	"(default as many as the supplied training set, 100% )",
  																	"PE", 1, "-PE"));	
  	newVector.addElement(new Option("\tMeta learner that produces the models to combine." +
  																	"(default is weka.classifiers.meta.Bagging)",
  																	"Z", 1, "-Z"));
  	Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }  
	/**
		* Parses a given list of options. <p/>
		*
		* Options after -- are passed to the designated classifier.<p>
		*
		* @param options the list of options as an array of strings
		* @throws Exception if an option is not supported
	*/
	public void setOptions(String[] options) throws Exception {
		String dataSize = Utils.getOption("PE", options);
		setPE(Integer.parseInt(dataSize));
    String metaClassifierName = Utils.getOption('Z', options);
    String classifierName = Utils.getOption('W', options);
    if (metaClassifierName.length() > 0) {
      setMetaClassifier((SingleClassifierEnhancer)AbstractClassifier.forName(metaClassifierName, null));
      m_MetaClassifier.setOptions(options);
    }
    if (classifierName.length() > 0) { 
      setClassifier(AbstractClassifier.forName(classifierName, options));
    } 
    // super.setOptions(options); omitted for problems
	}
	/**
	 * Gets the current settings of the Classifier.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String [] getOptions() {
		Vector<String> result = new Vector<String>();
		result.add("-PE");
		result.add("" + getPE());
		result.add("-Z");
		result.add("" + getMetaClassifier().getClass().getName());
		result.addAll(Arrays.asList(super.getOptions()));
		return result.toArray(new String[result.size()]);
	}
  /**
   * Combine Multiple Models.
   *
   * @param Training set
   * @throws Exception if the classifier could not be built successfully
   */	
  public void buildClassifier(Instances data) throws Exception {
  	int N = data.numInstances()*PE/100;
  	m_MetaClassifier.buildClassifier(data);
		Instances new_data =  new Instances(data, N); 
		Random rnd = new Random(super.getSeed()); // All experiments should be repeatable. Select a seed.
		for( int i = 0; i<N; ++i){
			Instance rnd_inst = data.instance(rnd.nextInt(data.numInstances()));
			double clsLabel = m_MetaClassifier.classifyInstance(rnd_inst);
			rnd_inst.setClassValue(clsLabel);
			new_data.add(rnd_inst);
		}
		m_Classifier.buildClassifier(new_data);
  }
  /**
   * Classifies the given test instance.
   * 
   * @param instance the instance to be classified
   * @return the predicted most likely class for the instance or 
   * Instance.missingValue() if no prediction is made
   * @exception Exception if an error occurred during the prediction
   */
  public double classifyInstance(Instance instance) throws Exception {
  	return m_Classifier.classifyInstance(instance);
  }
  /**
   * Main method for testing this class.
   *
   * @param argv the options
   */
  public String toString(){
  	String text = "Combined model builded with a generated training set big the " +
  								PE + "% of the training data.\n\n" +
  								"Base classifier: "+m_Classifier.getClass()+"\n" + 
  								"Meta classifier: "+m_MetaClassifier.getClass()+"\n\n";
  	text += m_Classifier;
  	return text;
  }
  public static void main(String [] argv) {
    runClassifier(new CombineMultipleModels(), argv);
  }
}