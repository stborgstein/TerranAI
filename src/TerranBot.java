import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;


/**
 * IAIE 17-18 UC3M
 * @author Nerea Luis
 *
 */
public class TerranBot extends DefaultBWListener {


	private Mirror mirror = new Mirror();

	private Game game;

	private Player self;

	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	@Override
	public void onUnitCreate(Unit unit) {
		System.out.println("**UNIDAD EN PROCESO: " + unit.getType());
	}

	@Override
	public void onStart() {

		game = mirror.getGame();
		self = game.self();

		game.enableFlag(1);
		game.setLocalSpeed(10);

		//La clase BWTA es la que contiene informacion y llamadas al mapa
		//La primera vez que se ejecuta el bot sobre un mapa tarda siempre unos minutos en procesarlo, es normal
		System.out.println("Analyzing map...");
		BWTA.readMap();
		BWTA.analyze();


		System.out.println("Map data ready");

		//int i = 0;
		// for(BaseLocation baseLocation : BWTA.getBaseLocations()){
		// 	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
		// 	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
		//		System.out.print(position + ", ");
		// 	}
		// 	System.out.println();
		// }

	}

	@Override
	public void onFrame() {

		//game.setTextSize(10); Podemos modificar parametros de la interfaz del juego
		game.drawTextScreen(10, 10, "Jugando como " + self.getName() + " - " + self.getRace());

		StringBuilder units = new StringBuilder("Mis unidades:\n");

		//Esta es una forma de iterar sobre las unidades, aunque lo mejor es crear listas propias y manejarlas << importante
		for (Unit myUnit : self.getUnits()) {
			units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

			//ejemplo de regla 'si la unidad encontrada es un centro de mando y tengo mas de 50 minerales, crear un trabajador'
			if (myUnit.getType() == UnitType.Terran_Command_Center && self.minerals() >= 50) {
				myUnit.train(UnitType.Terran_SCV);
			}
			
			//'si la unidad es un trabajor y esta parado, mandarlo a la mina de mineral mas cercana'
			if (myUnit.getType().isWorker() && myUnit.isIdle()) {
				Unit closestMineral = null;

				//busca la mina mas cercana. Las minas de mineral y vespeni asi como la refineria son neutral units
				for (Unit neutralUnit : game.neutral().getUnits()) {
					if (neutralUnit.getType().isMineralField()) {
						if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
							closestMineral = neutralUnit;
						}
					}
				}

				//si se encuentra la mina, mandar al trabajador alli
				if (closestMineral != null) {
					myUnit.gather(closestMineral, false);
				}
			}
		}

		//esta llamada es la que muestra la informacion de las unidades en la pantalla
		game.drawTextScreen(10, 25, units.toString());
	}

	@Override
	public void onEnd(boolean arg0) {
		System.out.println("Fin de la partida");
	}

	@Override
	public void onNukeDetect(Position arg0) {

	}

	@Override
	public void onPlayerDropped(Player arg0) {

	}

	@Override
	public void onPlayerLeft(Player arg0) {

	}

	@Override
	public void onReceiveText(Player arg0, String arg1) {

	}

	@Override
	public void onSaveGame(String arg0) {

	}

	@Override
	public void onSendText(String arg0) {

	}

	@Override
	public void onUnitComplete(Unit arg0) {
		Unit u = arg0;     
		System.out.println("\\UNIDAD COMPLETADA: "+u.getType());

	}

	@Override
	public void onUnitDestroy(Unit arg0) {

	}

	@Override
	public void onUnitDiscover(Unit arg0) {

	}

	@Override
	public void onUnitEvade(Unit arg0) {

	}

	@Override
	public void onUnitHide(Unit arg0) {

	}

	@Override
	public void onUnitMorph(Unit arg0) {

	}

	@Override
	public void onUnitRenegade(Unit arg0) {

	}

	@Override
	public void onUnitShow(Unit arg0) {

	}


	public static void main(String[] args) {
		new TerranBot().run();
	}
}