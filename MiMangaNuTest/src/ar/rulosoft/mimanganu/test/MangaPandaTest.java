package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.MangaPanda;

public class MangaPandaTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new MangaPanda();
		manga = new Manga(server.getServerID(), "Abias", "http://www.mangapanda.com/a-bias-girl", false);
		capitulo = new Capitulo("uno", "http://www.mangapanda.com/a-bias-girl/5");
	}

}
