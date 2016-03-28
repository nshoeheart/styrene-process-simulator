import java.lang.Math;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Schuchert
 * @version February 11, 2016
 *
 * This program calculates the optimal operating conditions for a styrene production process.
 */
public class SWDAL3 {
	/**
	 * Units:
	 *
	 * Steam:EB ratio as molar ratio
	 * Pressure in bar
	 * Temperature in K
	 */

	static Double optimalEconomicPotential = null;
	static double optimalSteamRatio;
	static double optimalPressure;
	static double optimalTemp;
	static StyreneProcess optimalStyreneProcess;

	static double steamRatioLow;
	static double steamRatioHigh;
	static double steamRatioStep;

	static double pressureLow;
	static double pressureHigh;
	static double pressureStep;

	static double tempLow;
	static double tempHigh;
	static double tempStep;

	final static double S_PROD_RATE = 80000d / 8350d * 1000d / 104.15 * 1000d; // in mol/hr

	public static void main(String[] args) {
		// params in format: (low, high, step)
		setSteamRatioParams(6d, 6d, 0.5);
		setPressureParams(0.4, 0.4, 0.1);
		setTempParams(800d, 950d, 5d);

		System.out.println("\nBeginning Styrene Process Simulation\n");

		StyreneProcess styreneProcess;
		List<ProcessEconSummary> processSummaries = new ArrayList<ProcessEconSummary>();

		// Begin iteration over all possibilities of process operating conditions
		for (double steamRatio = steamRatioLow; steamRatio <= steamRatioHigh; steamRatio += steamRatioStep) {
			System.out.println("Checking pressures and temperatures for steam ratio of: " + steamRatio);
			
			for (double pressure = pressureLow; pressure <= (pressureHigh + pressureStep/2d); pressure += pressureStep) {
				System.out.println("\tChecking temperatures for pressure of: " + pressure + " bar");

				for (double temp = tempLow; temp <= tempHigh; temp += tempStep) {
					//System.out.println("\t\tChecking values for temperature of: " + temp + " K");

					styreneProcess = findOptimalFlowRates(steamRatio, pressure, temp);
					double economicPotential = styreneProcess.getEconomicPotential();

					// Check if the conditions tested have been the best economically so far
					if ((optimalEconomicPotential == null) || (economicPotential > optimalEconomicPotential)) {
						optimalEconomicPotential = economicPotential;
						optimalSteamRatio = steamRatio;
						optimalPressure = pressure;
						optimalTemp = temp;
						optimalStyreneProcess = new StyreneProcess(styreneProcess);
					}

					ProcessEconSummary pes = new ProcessEconSummary(steamRatio, pressure, temp, economicPotential);
					processSummaries.add(pes);
				}
			}
		}

		System.out.println("\n Saving results to csv file\n");
		//writeEconomicPotentialCSVFile(processSummaries);

		System.out.println("\nOptimal Economic Potential:\t" + String.format("%.2f", optimalEconomicPotential) + " $/hr");
		System.out.println("Optimal Steam Ratio:\t\t" + optimalSteamRatio + " mol Steam : mol EB");
		System.out.println("Optimal Pressure:\t\t" + optimalPressure + " bar");
		System.out.println("Optimal Temperature:\t\t" + optimalTemp + " K");
		System.out.println("\n" + optimalStyreneProcess.toString());


		double keq = getKeq(optimalTemp);
		double selectivity = getSelectivity(optimalTemp);
		double[] xs = getExtentsOfReaction(optimalStyreneProcess.getReactorFeed().getTotalFeed(), keq, optimalStyreneProcess.getReactorFeed().getFeedEB(), selectivity, optimalPressure);
		System.out.println("X1 = " + (xs[0] / optimalStyreneProcess.getReactorFeed().getFeedEB()));
		System.out.println("X2 = " + (xs[1] / optimalStyreneProcess.getReactorFeed().getFeedEB()));
	}

	/**
	 * Converge on the optimal flow rates of all process streams for the given operating conditions
	 * 
	 * @param steamRatio - Molar ratio of steam to ethylbenzene input to styrene reactor
	 * @param pressure - Styrene reactor operating pressure
	 * @param temp - Styrene reactor operating temperature
	 * @return a StyreneProcess object containing all flow information
	 */
	static StyreneProcess findOptimalFlowRates(double steamRatio, double pressure, double temp) {
		double keq = getKeq(temp);
		double selectivity = getSelectivity(temp);

		double initialProcessFeed = 50000; // initial guess of total moles fed to process (NOT including recycle)
		double intialRecycledEB = 100; // initial guess of moles of ethylbenzene recycled 

		ReactorFeed reactorFeed = new ReactorFeed(initialProcessFeed, intialRecycledEB, steamRatio);
		double[] xs = getExtentsOfReaction(reactorFeed.getTotalFeed(), keq, reactorFeed.getFeedEB(), selectivity, pressure);
		ReactorProduct reactorProd = new ReactorProduct(reactorFeed, xs[0], xs[1]);

		convergeRecycleStream(reactorFeed, reactorProd, keq, selectivity, pressure);

		while (Math.abs(S_PROD_RATE - reactorProd.getProdS()) > S_PROD_RATE/100000) { // While more than 0.001% off of required styrene production rate
			if (reactorProd.getProdS() < S_PROD_RATE) {
				reactorFeed.changeProcessFeedBasis(1); //todo make these changes proportional to distance away from desired production rate somehow
			} else {
				reactorFeed.changeProcessFeedBasis(-1);
			}

			xs = getExtentsOfReaction(reactorFeed.getTotalFeed(), keq, reactorFeed.getFeedEB(), selectivity, pressure);
			reactorProd.setExtentsOfReaction(xs[0], xs[1]);

			convergeRecycleStream(reactorFeed, reactorProd, keq, selectivity, pressure);
		}

		return new StyreneProcess(reactorFeed, reactorProd);
	}

	/**
	 * Iterates to converge on the flow rate of the recycle stream for a set process feed rate
	 * 
	 * @param reactorFeed - a ReactorFeed object containing data on flow rates of reactor feed components
	 * @param reactorProd - a ReactorProduct object containing data on flow rates of reactor products
	 * @param keq - equilibrium K value of the styrene reaction
	 * @param selectivity - selectivity of moles toluene produced in side reaction vs. moles styrene produced in main reaction
	 * @param pressure - operating pressure of styrene reactor
	 */
	static void convergeRecycleStream(ReactorFeed reactorFeed, ReactorProduct reactorProd, double keq, double selectivity, double pressure) {
		while ((Math.abs(reactorFeed.getRecycledEB() / reactorProd.getProdEB() - 1)) > 0.00001) { // while the streams are more than 0.001% off of each other
			reactorFeed.setRecycledEB(reactorProd.getProdEB());
			double[] xs = getExtentsOfReaction(reactorFeed.getTotalFeed(), keq, reactorFeed.getFeedEB(), selectivity, pressure);
			reactorProd.setExtentsOfReaction(xs[0], xs[1]);
		}
	}

	/**
	 * Calculates the equilibrium K value of the styrene reaction
	 *
	 * @param temp - operating temperature of styrene reactor
	 */
	static double getKeq(double temp) {
		return Math.exp(15.5408 - 14852.6/temp);
	}

	/**
	 * Calculates the selectivity of toluene to styrene. Equation used for calculation was modeled on a power fit to the given selectivity data.
	 *
	 * @param temp - operating temperature of styrene reactor
	 */
	static double getSelectivity(double temp) {
		double coeff = 2.8001159389E-45;
		double exp = 14.669631361;
		return coeff*Math.pow(temp, exp);
	}

	/**
	 * Gets the actual extents of reaction for main styrene reaction and toluene side-reaction
	 * 
	 * @param f - total moles in reactor feed (combination of raw feed and recycle streams)
	 * @param k - the equilibrium K value for the main reaction
	 * @param eb - total moles of ethylbenzene entering the reactor (from both raw feed and recycle)
	 * @param y - the selectivity of toluene to styrene
	 * @param p - the reactor pressure in bar
	 *
	 * @return double[] xs:
	 *			xs[0] = extent of reaction (in moles) of main styrene production: EB -> S + H
	 *			xs[1] = extent of reaction (in moles) of side toluene reaction: EB + H -> T + M
	 */
	static double[] getExtentsOfReaction(double f, double k, double eb, double y, double p) {
		double x1Ideal = (Math.sqrt(4*f*k*eb*(k*y + k - p*y + p) + Math.pow(f*k*y + f*k - k*eb, 2)) - f*k*y - f*k + k*eb)/(2*(k*y + k - p*y + p));
		double x1Actual = 0.8 * x1Ideal; // actual extent of reaction is 0.8 of equilibrium value
		double x2Actual = y * x1Actual; // selectivity defined as y=x2/x1, so x2 = y*x1

		double[] xs = {x1Actual, x2Actual};
		return xs;
	}

	private static void writeEconomicPotentialCSVFile(List<ProcessEconSummary> processes) {
		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter("StyreneEconSummary.csv");
			fileWriter.append(ProcessEconSummary.CSV_HEADER);

			for (ProcessEconSummary process : processes) {
				fileWriter.append(process.getCSVLine());
			}
		} catch (Exception e) {
			System.out.println("Error writing CSV file");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error flushing/closing CSV file writer");
				e.printStackTrace();
			}
		}
	}

	private static void setSteamRatioParams(double low, double high, double step) {
		steamRatioLow = low;
		steamRatioHigh = high;
		steamRatioStep = step;
	}

	private static void setPressureParams(double low, double high, double step) {
		pressureLow = low;
		pressureHigh = high;
		pressureStep = step;
	}

	private static void setTempParams(double low, double high, double step) {
		tempLow = low;
		tempHigh = high;
		tempStep = step;
	}
}