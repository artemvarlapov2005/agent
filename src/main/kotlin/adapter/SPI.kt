package org.matkini.adapter

import org.matkini.IpAddress

interface NetworkManagerAdapter {
    fun getAllowedSubNets() : List<IpAddress>
}