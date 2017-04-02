package com.cht.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiOutput.Enabled;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		AnsiOutput.setEnabled(Enabled.DETECT);
		SpringApplication.run(Main.class, args);
	}
}
