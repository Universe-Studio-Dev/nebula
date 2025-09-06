package github.universe.studio.nebula.bungee.others;

import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 05/09/2025
 * @project nebula
 * @file FriendManager
 */
public class FriendManager {
    private final Map<UUID, Set<UUID>> friendLists = new HashMap<>();
    private final Map<UUID, Set<UUID>> pendingRequests = new HashMap<>();
    private final ConfigManager configManager;

    private static final String CANNOT_ADD_SELF = "&cYou cannot add yourself as a friend!";
    private static final String ALREADY_FRIEND = "&c%player% is already your friend!";
    private static final String REQUEST_ALREADY_SENT = "&cYou already sent a friend request to %player%!";
    private static final String REQUEST_SENT = "&aFriend request sent to %player%!";
    private static final String REQUEST_RECEIVED = "&a%player% sent you a friend request! Use /friend accept %sender% or /friend deny %sender%";
    private static final String NO_REQUEST = "&cNo friend request from %player%!";
    private static final String REQUEST_ACCEPTED = "&aYou accepted %player%'s friend request!";
    private static final String REQUEST_DENIED = "&aYou denied %player%'s friend request!";
    private static final String REQUEST_DENIED_BY = "&c%player% denied your friend request!";
    private static final String ADDED = "&a%player% is now your friend!";
    private static final String NOT_FRIEND = "&c%player% is not your friend!";
    private static final String REMOVED = "&aRemoved %player% from your friends!";
    private static final String REMOVED_BY = "&c%player% removed you from their friends!";
    private static final String LIST_EMPTY = "&cYou have no friends!";
    private static final String LIST_HEADER = "&aYour Friends:";
    private static final String LIST_ENTRY = "&a- %player%: %status%";
    private static final String OFFLINE = "&c%player% is offline!";
    private static final String JOINED = "&aJoined %player%'s server!";
    private static final String CONNECTED = "&a%player% connected to %server%!";
    private static final String DISCONNECTED = "&c%player% disconnected!";

    public FriendManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean sendFriendRequest(ProxiedPlayer sender, ProxiedPlayer target) {
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();
        if (senderId.equals(targetId)) {
            sender.sendMessage(CC.translate(CANNOT_ADD_SELF));
            return false;
        }
        Set<UUID> friends = friendLists.computeIfAbsent(senderId, k -> new HashSet<>());
        if (friends.contains(targetId)) {
            sender.sendMessage(CC.translate(ALREADY_FRIEND.replace("%player%", target.getName())));
            return false;
        }
        Set<UUID> requests = pendingRequests.computeIfAbsent(targetId, k -> new HashSet<>());
        if (requests.contains(senderId)) {
            sender.sendMessage(CC.translate(REQUEST_ALREADY_SENT.replace("%player%", target.getName())));
            return false;
        }
        requests.add(senderId);
        savePendingRequests(targetId);
        sender.sendMessage(CC.translate(REQUEST_SENT.replace("%player%", target.getName())));
        target.sendMessage(CC.translate(REQUEST_RECEIVED.replace("%player%", sender.getName()).replace("%sender%", sender.getName())));
        return true;
    }

    public boolean acceptFriendRequest(ProxiedPlayer player, ProxiedPlayer sender) {
        UUID playerId = player.getUniqueId();
        UUID senderId = sender.getUniqueId();
        Set<UUID> requests = pendingRequests.get(playerId);
        if (requests == null || !requests.contains(senderId)) {
            player.sendMessage(CC.translate(NO_REQUEST.replace("%player%", sender.getName())));
            return false;
        }
        Set<UUID> playerFriends = friendLists.computeIfAbsent(playerId, k -> new HashSet<>());
        Set<UUID> senderFriends = friendLists.computeIfAbsent(senderId, k -> new HashSet<>());
        playerFriends.add(senderId);
        senderFriends.add(playerId);
        requests.remove(senderId);
        if (requests.isEmpty()) {
            pendingRequests.remove(playerId);
        }
        saveFriends(playerId);
        saveFriends(senderId);
        savePendingRequests(playerId);
        player.sendMessage(CC.translate(REQUEST_ACCEPTED.replace("%player%", sender.getName())));
        sender.sendMessage(CC.translate(ADDED.replace("%player%", player.getName())));
        return true;
    }

    public boolean denyFriendRequest(ProxiedPlayer player, ProxiedPlayer sender) {
        UUID playerId = player.getUniqueId();
        UUID senderId = sender.getUniqueId();
        Set<UUID> requests = pendingRequests.get(playerId);
        if (requests == null || !requests.contains(senderId)) {
            player.sendMessage(CC.translate(NO_REQUEST.replace("%player%", sender.getName())));
            return false;
        }
        requests.remove(senderId);
        if (requests.isEmpty()) {
            pendingRequests.remove(playerId);
        }
        savePendingRequests(playerId);
        player.sendMessage(CC.translate(REQUEST_DENIED.replace("%player%", sender.getName())));
        sender.sendMessage(CC.translate(REQUEST_DENIED_BY.replace("%player%", player.getName())));
        return true;
    }

    public boolean removeFriend(ProxiedPlayer player, ProxiedPlayer target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();
        Set<UUID> friends = friendLists.get(playerId);
        if (friends == null || !friends.contains(targetId)) {
            player.sendMessage(CC.translate(NOT_FRIEND.replace("%player%", target.getName())));
            return false;
        }
        friends.remove(targetId);
        Set<UUID> targetFriends = friendLists.get(targetId);
        if (targetFriends != null) {
            targetFriends.remove(playerId);
            saveFriends(targetId);
        }
        if (friends.isEmpty()) {
            friendLists.remove(playerId);
        }
        saveFriends(playerId);
        player.sendMessage(CC.translate(REMOVED.replace("%player%", target.getName())));
        if (target.isConnected()) {
            target.sendMessage(CC.translate(REMOVED_BY.replace("%player%", player.getName())));
        }
        return true;
    }

    public void listFriends(ProxiedPlayer player) {
        UUID playerId = player.getUniqueId();
        Set<UUID> friends = friendLists.getOrDefault(playerId, Collections.emptySet());
        if (friends.isEmpty()) {
            player.sendMessage(CC.translate(LIST_EMPTY));
            return;
        }
        player.sendMessage(CC.translate(LIST_HEADER));
        for (UUID friendId : friends) {
            ProxiedPlayer friend = ProxyServer.getInstance().getPlayer(friendId);
            String status = friend != null ? "&aOnline &7(" + friend.getServer().getInfo().getName() + ")" : "&cOffline";
            player.sendMessage(CC.translate(LIST_ENTRY
                    .replace("%player%", friend != null ? friend.getName() : "Unknown")
                    .replace("%status%", status)));
        }
    }

    public boolean joinFriend(ProxiedPlayer player, ProxiedPlayer target) {
        UUID targetId = target.getUniqueId();
        Set<UUID> friends = friendLists.get(player.getUniqueId());
        if (friends == null || !friends.contains(targetId)) {
            player.sendMessage(CC.translate(NOT_FRIEND.replace("%player%", target.getName())));
            return false;
        }
        if (target.getServer() == null) {
            player.sendMessage(CC.translate(OFFLINE.replace("%player%", target.getName())));
            return false;
        }
        player.connect(target.getServer().getInfo());
        player.sendMessage(CC.translate(JOINED.replace("%player%", target.getName())));
        return true;
    }

    public void notifyFriendConnect(ProxiedPlayer player) {
        if (!ConfigManager.getConfig().getBoolean("friend.notify-connect", true)) {
            return;
        }
        UUID playerId = player.getUniqueId();
        for (Map.Entry<UUID, Set<UUID>> entry : friendLists.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                ProxiedPlayer friend = ProxyServer.getInstance().getPlayer(entry.getKey());
                if (friend != null) {
                    friend.sendMessage(CC.translate(CONNECTED
                            .replace("%player%", player.getName())
                            .replace("%server%", player.getServer().getInfo().getName())));
                }
            }
        }
    }

    public void notifyFriendDisconnect(ProxiedPlayer player) {
        if (!ConfigManager.getConfig().getBoolean("friend.notify-disconnect", true)) {
            return;
        }
        UUID playerId = player.getUniqueId();
        for (Map.Entry<UUID, Set<UUID>> entry : friendLists.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                ProxiedPlayer friend = ProxyServer.getInstance().getPlayer(entry.getKey());
                if (friend != null) {
                    friend.sendMessage(CC.translate(DISCONNECTED
                            .replace("%player%", player.getName())));
                }
            }
        }
    }

    public void loadFriends() {
        Configuration friendsConfig = ConfigManager.getFriends();
        Configuration players = friendsConfig.getSection("players");
        if (players == null) {
            return;
        }
        for (String playerId : players.getKeys()) {
            try {
                UUID uuid = UUID.fromString(playerId);
                List<String> friendIds = players.getStringList(playerId + ".friends");
                Set<UUID> friends = friendIds.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());
                friendLists.put(uuid, friends);
                List<String> requestIds = players.getStringList(playerId + ".pending-requests");
                Set<UUID> requests = requestIds.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());
                if (!requests.isEmpty()) {
                    pendingRequests.put(uuid, requests);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void saveFriends(UUID playerId) {
        Configuration friendsConfig = ConfigManager.getFriends();
        Set<UUID> friends = friendLists.get(playerId);
        if (friends == null || friends.isEmpty()) {
            friendsConfig.set("players." + playerId.toString() + ".friends", null);
        } else {
            List<String> friendIds = friends.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            friendsConfig.set("players." + playerId.toString() + ".friends", friendIds);
        }
        configManager.saveFriends();
    }

    private void savePendingRequests(UUID playerId) {
        Configuration friendsConfig = ConfigManager.getFriends();
        Set<UUID> requests = pendingRequests.get(playerId);
        if (requests == null || requests.isEmpty()) {
            friendsConfig.set("players." + playerId.toString() + ".pending-requests", null);
        } else {
            List<String> requestIds = requests.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            friendsConfig.set("players." + playerId.toString() + ".pending-requests", requestIds);
        }
        configManager.saveFriends();
    }
}