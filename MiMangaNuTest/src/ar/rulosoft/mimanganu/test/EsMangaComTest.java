package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.EsMangaCom;

public class EsMangaComTest extends TestBase {

	@Before
	public void setUp() throws Exception {
		server = new EsMangaCom();
		manga = new Manga(server.getServerID(), "Abias", "http://esmanga.com/manga/a-bias-girl", false);
		capitulo = new Capitulo("uno", "http://esmanga.com/manga/a-bias-girl/c5");
	}
	
}
