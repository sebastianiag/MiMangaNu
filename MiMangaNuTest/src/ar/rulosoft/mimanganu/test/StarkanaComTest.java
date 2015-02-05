package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.StarkanaCom;

public class StarkanaComTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new StarkanaCom();
		manga = new Manga(server.getServerID(), "Abias", "http://starkana.jp/manga/B/Bleach", false);
		capitulo = new Capitulo("uno", "http://starkana.jp/manga/B/Bleach/chapter/614");
	}

}
