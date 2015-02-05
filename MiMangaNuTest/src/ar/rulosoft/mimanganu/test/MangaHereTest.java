package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.MangaHere;

public class MangaHereTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new MangaHere();
		manga = new Manga(server.getServerID(), "Abias", "http://www.mangahere.co/manga/a_bias_girl/", false);
		capitulo = new Capitulo("uno", "http://www.mangahere.co/manga/a_bias_girl/v01/c006/");
	}

}
