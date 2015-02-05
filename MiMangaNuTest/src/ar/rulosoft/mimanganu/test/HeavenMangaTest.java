package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.HeavenMangaCom;

public class HeavenMangaTest extends TestBase{

	@Before
	public void setUp() throws Exception {
		server = new HeavenMangaCom();
		manga = new Manga(server.getServerID(), "Akame", "http://heavenmanga.com/akame-ga-kill/", false);
		capitulo = new Capitulo("uno", "http://heavenmanga.com/akame-ga-kill-54.html");
	}

}
