import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
/**
 * 
 * @author sridh
 * KNN-Classifier
 */
public class knnClassifier {

	public static void main(String[] args) throws IOException {
		
		//Requesting user to enter the K -iterations as input
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter the value of k.");
		int k = scan.nextInt();
		System.out.println("Select the distance formula. \n\t1.Euclidean\n\t2.Manhattan");
		//User to choose type of distance formula 
		boolean IsEuclidean = scan.nextInt() == 1 ? true : false;
		evaluate(k, IsEuclidean);
		scan.close();
		}

	public static String predictSpecie(List<Iris> dataset, Integer k, Double sepalLength, Double sepalWidth,
			Double petalLength, Double petalWidth, boolean isManhattan) {
		// calculate distance for each sample in dataset
		Iris unknownIris = new Iris(sepalLength, sepalWidth, petalLength, petalWidth, null);
		List<Score> scores = new ArrayList<Score>();
		for (Iris iris : dataset)
			scores.add(new Score(unknownIris.distance(iris, isManhattan), iris.specie));
		Collections.sort(scores, Score.COMPARATOR);

		// count occurences for K nearest neighbor
		Map<String, Integer> occurenceCount = new HashMap<String, Integer>();
		for (Integer i = 0; i < scores.size(); i++) {
			String specie = scores.get(i).specie;
			if (occurenceCount.containsKey(specie)) {
				occurenceCount.put(specie, occurenceCount.get(specie) + 1);
			} else {
				occurenceCount.put(specie, 1);
			}

			if (i >= k - 1) {
				break;
			}
		}

		// find the most frequent occurence
		String mostFrequentSpecie = null;
		Integer nbOccurence = 0;
		for (Entry<String, Integer> entry : occurenceCount.entrySet()) {
			if (nbOccurence < entry.getValue()) {
				nbOccurence = entry.getValue();
				mostFrequentSpecie = entry.getKey();
			}
		}
		return mostFrequentSpecie;
	}

	public static List<Iris> loadDataset(String TextFile) {
		List<Iris> dataset = new ArrayList<Iris>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			//Parsing the Textfile into ArrayList. 
			br = new BufferedReader(new FileReader(TextFile));
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					String[] cell = line.split(cvsSplitBy);
					dataset.add(new Iris(Double.parseDouble(cell[0]), Double.parseDouble(cell[1]),
							Double.parseDouble(cell[2]), Double.parseDouble(cell[3]), cell[4]));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return dataset;
	}

	static class Score {
		public static final Comparator<Score> COMPARATOR = new Comparator<Score>() {
			@Override
			public int compare(Score s1, Score s2) {
				return s1.score.compareTo(s2.score);
			}
		};
		public Double score;
		public String specie;

		public Score(Double score, String specie) {
			this.score = score;
			this.specie = specie;
		}
	}

	static class Iris {
		public Double sepalLength;
		public Double sepalWidth;
		public Double petalLength;
		public Double petalWidth;
		public String specie;

		public Iris(Double sepalLength, Double sepalWidth, Double petalLength, Double petalWidth, String specie) {
			this.sepalLength = sepalLength;
			this.sepalWidth = sepalWidth;
			this.petalLength = petalLength;
			this.petalWidth = petalWidth;
			this.specie = specie;
		}

		public Double distance(Iris that, boolean IsEuclidean) {
			if (IsEuclidean)
				return Math.sqrt(Math.pow(sepalLength - that.sepalLength, 2) + Math.pow(sepalWidth - that.sepalWidth, 2)
				+ Math.pow(petalLength - that.petalLength, 2) + Math.pow(petalWidth - that.petalWidth, 2));
			else
				return Math.abs(sepalLength - that.sepalLength) + Math.abs(sepalWidth - that.sepalWidth)
						+ Math.abs(petalLength - that.petalLength) + Math.abs(petalWidth - that.petalWidth);			
		}

		@Override
		public String toString() {
			return sepalLength.toString() + " " + sepalWidth.toString() + " " + petalLength.toString() + " "
					+ petalWidth.toString() + " " + specie;
		}
	}

	public static void evaluate(int k, boolean isManhattan) throws IOException {
		List<Iris> trainingDataset = loadDataset("IM1_Training.txt");
		List<Iris> testingdataset = loadDataset("IM2_Testing.txt");
		BufferedWriter outputDoc = new BufferedWriter(new FileWriter(new File("output.txt")));
		String prediction, output = "";
		Float success = 0.0f;
		// for each example in testing training set, predict the classification
		for (Iris iris : testingdataset) {
			
			//Parsing the testset line by line and comparing the KNN distance
			prediction = predictSpecie(trainingDataset, k, iris.sepalLength, iris.sepalWidth, iris.petalLength,
					iris.petalWidth, isManhattan);
			output += iris.toString() + " " + prediction + "\n";
			if (prediction.equals(iris.specie))
				success++;
		}
		outputDoc.write(output);
		outputDoc.close();
		//Displaying the accuracy with respect to the success from the predicted result over test dataset.
		System.out.println("Accuracy of the model: " + (success / testingdataset.size()) * 100 + "%");
	}
}