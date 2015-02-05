package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.SubManga;

public class SubMangaTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new SubManga();
		manga = new Manga(server.getServerID(), "Abias", "http://submanga.com/Bleach", false);
		capitulo = new Capitulo("uno", "http://submanga.com/c/239643");
	}

}
