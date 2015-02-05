package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.MangaFox;

public class MangaFoxTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new MangaFox();
		manga = new Manga(server.getServerID(), "Abias", "http://mangafox.me/manga/a_bias_girl/", false);
		capitulo = new Capitulo("uno", "http://mangafox.me/manga/a_bias_girl/v01/c005/1.html");
	}

}
