package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.EsMangaHere;

public class EsMangaHereTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new EsMangaHere();
		manga = new Manga(server.getServerID(), "Abias", "http://es.mangahere.co/manga/a_bias_girl/", false);
		capitulo = new Capitulo("uno", "http://es.mangahere.co/manga/a_bias_girl/c5/");
	}

}
