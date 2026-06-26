/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.template.config;

import java.io.File;

/**
 * Interface for loading test data configuration from files.
 * Implementations support different file formats (JSON, XML).
 */
public interface ConfigLoader {

    /**
     * Load a TestDataConfig from the given file.
     *
     * @param configFile the configuration file to load
     * @return the parsed TestDataConfig
     * @throws ConfigLoadException if the file cannot be parsed
     */
    TestDataConfig load(File configFile) throws ConfigLoadException;

    /**
     * Check whether this loader supports the given file format.
     *
     * @param configFile the file to check
     * @return true if this loader can handle the file
     */
    boolean supports(File configFile);
}