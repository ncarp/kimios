/*
 * Kimios - Document Management System Software
 * Copyright (C) 2008-2012  DevLib'
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kimios.kernel.dms;

import java.io.Serializable;

/***
 *
 *
 *
 */
public class MetaValueBeanPK implements Serializable
{
    protected long documentVersionUid;

    protected long metaUid;

    public long getDocumentVersionUid()
    {
        return documentVersionUid;
    }

    public void setDocumentVersionUid(long documentVersionUid)
    {
        this.documentVersionUid = documentVersionUid;
    }

    public long getMetaUid()
    {
        return metaUid;
    }

    public void setMetaUid(long metaUid)
    {
        this.metaUid = metaUid;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetaValueBeanPK that = (MetaValueBeanPK) o;

        if (documentVersionUid != that.documentVersionUid) {
            return false;
        }
        if (metaUid != that.metaUid) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (documentVersionUid ^ (documentVersionUid >>> 32));
        result = 31 * result + (int) (metaUid ^ (metaUid >>> 32));
        return result;
    }
}
