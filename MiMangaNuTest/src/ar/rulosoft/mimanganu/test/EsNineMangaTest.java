package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.EsNineMangaCom;

public class EsNineMangaTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new EsNineMangaCom();
		manga = new Manga(server.getServerID(), "Abias", "http://es.ninemanga.com/manga/Bleach.html", false);
		capitulo = new Capitulo("uno", "http://es.ninemanga.com/chapter/Bleach/685605.html");
	}

}
