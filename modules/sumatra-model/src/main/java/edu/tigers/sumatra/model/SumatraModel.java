/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import edu.tigers.moduli.Moduli;
import edu.tigers.moduli.exceptions.DependencyException;
import edu.tigers.moduli.exceptions.LoadModulesException;


/**
 * The model of the application.
 * It contains the low-level-methods and data.
 * You can use the methods in a Presenter for combining
 * to a business - logic.
 * In Sumatra the Model is entirely outsourced in moduli.
 * That means, that all low-level-methods are within separated modules.
 * The model make use of a Singleton - pattern,
 * so you can access the Model
 * with a simple SumatraModel.getInstance() .
 *
 * @author bernhard
 */
public final class SumatraModel extends Moduli
{
	private static final Logger log = LogManager.getLogger(SumatraModel.class.getName());

	// --- version ---
	private static final String VERSION = getVersionNameFromManifest();

	// --- singleton ---
	private static final SumatraModel INSTANCE = new SumatraModel();

	/**
	 * These {@link Properties} contain the information necessary for the application to run properly (e.g., file paths)
	 */
	private Properties userSettings = new Properties();

	// --- moduli config ---
	private static final String KEY_MODULI_CONFIG = SumatraModel.class.getName() + ".moduliConfig";
	/** */
	public static final String MODULI_CONFIG_PATH = "./config/moduli/";
	/**  */
	public static final String MODULI_CONFIG_FILE_DEFAULT = "sim.xml";

	// Application Properties
	private static final String CONFIG_SETTINGS_PATH = "./config/";


	private boolean productive = false;
	private boolean testMode = false;

	private boolean simulation = false;
	private String environment = "";


	/**
	 * Constructor from model.
	 * Initializes data which is kept by the model.
	 */
	private SumatraModel()
	{
		loadApplicationProperties();
		String prod = System.getProperty("productive");
		if (prod != null)
		{
			productive = Boolean.parseBoolean(prod);
		}
		String test = System.getProperty("testMode");
		if (test != null)
		{
			testMode = Boolean.parseBoolean(test);
		}
	}


	/**
	 * getInstance() - Singleton-pattern.
	 *
	 * @return the "one-and-only" instance of CSModel
	 */
	public static SumatraModel getInstance()
	{
		return INSTANCE;
	}


	/**
	 * Load application properties in two steps
	 */
	private void loadApplicationProperties()
	{
		userSettings = new Properties();
		final File uf = getUserPropertiesFile();
		if (!uf.exists())
		{
			return;
		}
		try (FileInputStream inUserSettings = new FileInputStream(uf))
		{
			userSettings.load(inUserSettings);
		} catch (final IOException err)
		{
			log.warn("Config: " + uf.getPath() + " could not be read, using default configs!", err);
		}
	}


	/**
	 * Load modules of the given config file. The config path is appended by this method and must not be prepended.
	 *
	 * @param configFileName the config file name
	 * @throws DependencyException
	 * @throws LoadModulesException
	 */
	@SuppressWarnings("squid:S1160") // throwing two exceptions, because this is only a proxy method
	public void loadModulesOfConfig(final String configFileName) throws DependencyException, LoadModulesException
	{
		super.loadModules(MODULI_CONFIG_PATH + configFileName);
		updateInternalStateFromGlobalConfig();
	}


	/**
	 * Load modules of the given config file. The config path is appended by this method and must not be prepended.
	 * Catch exceptions and continue normally
	 *
	 * @param configFileName the config file name
	 */
	public void loadModulesOfConfigSafe(final String configFileName)
	{
		super.loadModulesSafe(MODULI_CONFIG_PATH + configFileName);
		updateInternalStateFromGlobalConfig();
	}


	private void updateInternalStateFromGlobalConfig()
	{
		simulation = getGlobalConfiguration().getBoolean("simulation", false);
		environment = getGlobalConfiguration().getString("environment");
	}


	/**
	 * @return The {@link File} which represents the user-properties file
	 */
	private File getUserPropertiesFile()
	{
		final String userName = System.getProperty("user.name");
		final String filename = CONFIG_SETTINGS_PATH + userName + ".props";

		return new File(filename);
	}


	// --------------------------------------------------------------------------
	// --- moduli config --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the currentModuliConfig
	 */
	public String getCurrentModuliConfig()
	{
		return userSettings.getProperty(SumatraModel.KEY_MODULI_CONFIG, MODULI_CONFIG_FILE_DEFAULT);
	}


	/**
	 * @param currentModuliConfig the currentModuliConfig to set
	 */
	public void setCurrentModuliConfig(final String currentModuliConfig)
	{
		userSettings.setProperty(SumatraModel.KEY_MODULI_CONFIG, currentModuliConfig);
	}


	// --------------------------------------------------------------------------
	// --- app properties -------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * @return The applications {@link Properties}
	 */
	public Properties getUserSettings()
	{
		return userSettings;
	}


	/**
	 * Calls {@link Properties#setProperty(String, String)}
	 *
	 * @param key
	 * @param value
	 * @return The value which was associated with the given key before
	 */
	public String setUserProperty(final String key, final String value)
	{
		Object obj;
		if (value == null)
		{
			obj = userSettings.remove(key);
		} else
		{
			obj = userSettings.setProperty(key, value);
		}

		if (obj == null)
		{
			// no previous item
			return null;
		}
		if (!(obj instanceof String))
		{
			log.warn("Object '{}' (which has been associated to '{}') is no String!", obj, key);
			return null;
		}

		return (String) obj;
	}


	/**
	 * Calls {@link Properties#getProperty(String)}
	 *
	 * @param key
	 * @return The String associated with the given key
	 */
	public String getUserProperty(final String key)
	{
		return userSettings.getProperty(key);
	}


	/**
	 * Calls {@link Properties#getProperty(String)}
	 *
	 * @param key
	 * @param def
	 * @return The String associated with the given key
	 */
	public String getUserProperty(final String key, final String def)
	{
		String val = userSettings.getProperty(key);
		if (val == null)
		{
			return def;
		}
		return val;
	}


	/**
	 * Sumatra version
	 *
	 * @return
	 */
	public static String getVersion()
	{
		return VERSION;
	}


	/**
	 * @return if we are in productive (match) mode
	 */
	public final boolean isProductive()
	{
		return productive;
	}


	public void setTestMode(final boolean testMode)
	{
		this.testMode = testMode;
	}


	/**
	 * @return if we are in test mode
	 */
	public final boolean isTestMode()
	{
		return testMode;
	}


	/**
	 * save properties
	 */
	public void saveUserProperties()
	{
		if (userSettings.isEmpty())
		{
			return;
		}
		final File uf = getUserPropertiesFile();
		if (!uf.exists())
		{
			try
			{
				if (uf.createNewFile())
				{
					log.debug("new user file created");
				}
			} catch (IOException e)
			{
				log.error("Could not create properties file: " + uf.getAbsolutePath(), e);
			}
		}

		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(uf);
			userSettings.store(out, null);
		} catch (final IOException err)
		{
			log.warn("Could not write to " + uf.getPath() + ", configuration is not saved", err);
		}

		if (out != null)
		{
			try
			{
				out.close();
				log.trace("Saved configuration to: {}", uf.getPath());
			} catch (IOException e)
			{
				log.warn("Could not close {}, configuration is not saved", uf.getPath(), e);
			}
		}
	}


	/**
	 * @param lvl
	 */
	public static void changeLogLevel(final Level lvl)
	{
		Configurator.setRootLevel(lvl);
	}


	/**
	 * @return the current environment (Robocup, Lab, etc) as defined by moduli config
	 */
	public String getEnvironment()
	{
		return environment;
	}


	public boolean isSimulation()
	{
		return simulation;
	}


	private static String getVersionNameFromManifest()
	{
		InputStream manifestStream = SumatraModel.class.getClassLoader()
				.getResourceAsStream("edu/tigers/sumatra/model/metadata.properties");
		if (manifestStream != null)
		{
			try
			{
				final Properties properties = new Properties();
				properties.load(manifestStream);

				final String version = properties.getProperty("version", "unknown version")
						.replace("-SNAPSHOT", "");
				final String gitHash = properties.getProperty("git.hash", "");
				if (StringUtils.isNotBlank(gitHash))
				{
					return version + "_" + gitHash;
				}
				return version;
			} catch (IOException e)
			{
				log.error("Could not read manifest", e);
			}

		}
		return "";
	}
}
