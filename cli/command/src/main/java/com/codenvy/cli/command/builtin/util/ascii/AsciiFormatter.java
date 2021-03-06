/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin.util.ascii;

/**
 * @author Florent Benoit
 */
public interface AsciiFormatter {

    String getBorderLine(AsciiArrayInfo asciiArrayInfo);

    String getFormatter(AsciiArrayInfo asciiArrayInfo);

    String getTitleFormatter(AsciiArrayInfo asciiArrayInfo);

    String formatFormTitle(String name, AsciiFormInfo asciiFormInfo);

    String formatFormValue(String value, AsciiFormInfo asciiFormInfo);

}
