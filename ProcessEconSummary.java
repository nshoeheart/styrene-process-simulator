public class ProcessEconSummary {
	static final String CSV_HEADER = "SteamRatio(mol:mol EB),Pressure(bar),Temperature(K),EconomicPotential($/hr)\n";

	public double steamRatio; // mol Steam : mol EB entering reactor
	public double pressure; // reactor pressure in bar
	public double temperature; // reactor temperature in K
	public double economicPotential; // process economic potential in $/hr

	public ProcessEconSummary(double steamRatio, double pressure, double temperature, double economicPotential) {
		this.steamRatio = steamRatio;
		this.pressure = pressure;
		this.temperature = temperature;
		this.economicPotential = economicPotential;
	}

	public String getCSVLine() {
		return String.format("%f,%f,%f,%f\n", steamRatio, pressure, temperature, economicPotential);
	}
}