package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ItNineMangaCom;

public class ItNineMangaTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new ItNineMangaCom();
		manga = new Manga(server.getServerID(), "Bleach", "http://it.ninemanga.com/manga/Bleach.html", false);
		capitulo = new Capitulo("uno", "http://it.ninemanga.com/chapter/Bleach/223680.html");
	}

}
