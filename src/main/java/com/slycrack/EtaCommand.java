package com.slycrack;

import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.core.command.annotations.CommandExecutor;
import org.rusherhack.client.api.Globals;
import org.rusherhack.client.api.accessors.entity.IMixinLocalPlayer;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.system.INotificationManager;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class EtaCommand extends Command implements Globals {

    private double destinationX;
    private double destinationZ;
    private boolean destinationSet = false;
    private long lastUpdateTime;
    private double lastX;
    private double lastZ;
    private final Queue<Double> speedMeasurements = new LinkedList<>();
    private double currentSpeed;
    private ScheduledExecutorService scheduler;
    private final INotificationManager notificationManager;
    private final ReentrantLock lock = new ReentrantLock();
    private int notificationInterval = 0; // Default notification interval to 0

    public EtaCommand() {
        super("eta", "Sets destination and calculates ETA");
        notificationManager = RusherHackAPI.getNotificationManager();
    }

    private void startETAScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);

        // Speed update every 50ms
        scheduler.scheduleAtFixedRate(this::updateSpeed, 0, 50, TimeUnit.MILLISECONDS);

        // HUD update every second (1000ms)
        scheduler.scheduleAtFixedRate(this::updateHUD, 0, 1000, TimeUnit.MILLISECONDS);

        if (notificationInterval > 0) {
            scheduler.scheduleAtFixedRate(() -> {
                if (destinationSet) {
                    String etaMessage = calculateETA();
                    notificationManager.chat(etaMessage);
                }
            }, notificationInterval, notificationInterval, TimeUnit.SECONDS);
        }
    }

    private void updateHUD() {
    }

    @CommandExecutor(subCommand = "set")
    @CommandExecutor.Argument({"x", "z"})
    private String setDestination(double x, double z) {
        lock.lock();
        try {
            this.destinationX = x;
            this.destinationZ = z;
            this.destinationSet = true;

            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
            }

            IMixinLocalPlayer player = (IMixinLocalPlayer) mc.player;
            lastUpdateTime = System.currentTimeMillis();
            assert player != null;
            lastX = player.getLastX();
            lastZ = player.getLastZ();
            startETAScheduler();
        } catch (Exception e) {
            notificationManager.error("Failed to set destination: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return String.format("Destination set to (§a%.2f§r, §a%.2f§r).", x, z);
    }

    @CommandExecutor(subCommand = "eta")
    private String calculateETACommand() {
        return calculateETA();
    }

    @CommandExecutor(subCommand = "stop")
    private String stopETA() {
        lock.lock();
        try {
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
            destinationSet = false;
        } catch (Exception e) {
            notificationManager.error("Failed to stop ETA calculation: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return "ETA calculation stopped.";
    }

    @CommandExecutor(subCommand = "setInterval")
    @CommandExecutor.Argument("interval")
    private String setNotificationInterval(int interval) {
        lock.lock();
        try {
            this.notificationInterval = interval;
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
                if (destinationSet) {
                    startETAScheduler();
                }
            }
            if (interval == 0) {
                return "Notifications turned off.";
            } else {
                return "Notification interval set to " + interval + " seconds.";
            }
        } catch (Exception e) {
            notificationManager.error("Failed to set notification interval: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return "Notification interval set.";
    }

    public String calculateETA() {
        if (!destinationSet) {
            return "";
        }

        if (currentSpeed < 0.01) {
            return "ETA: N/A";
        }

        assert mc.player != null;
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();
        double distance = Math.sqrt(Math.pow(destinationX - playerX, 2) + Math.pow(destinationZ - playerZ, 2));

        // Calculate the direction vector to the destination
        double directionX = destinationX - playerX;
        double directionZ = destinationZ - playerZ;
        double directionMagnitude = Math.sqrt(directionX * directionX + directionZ * directionZ);
        directionX /= directionMagnitude;
        directionZ /= directionMagnitude;

        // Calculate the player's movement vector
        double movementX = playerX - lastX;
        double movementZ = playerZ - lastZ;
        double movementMagnitude = Math.sqrt(movementX * movementX + movementZ * movementZ);
        movementX /= movementMagnitude;
        movementZ /= movementMagnitude;

        // Calculate the dot product to check if moving towards or away from the destination
        double dotProduct = (directionX * movementX) + (directionZ * movementZ);
        if (dotProduct < 0) {
            currentSpeed = -currentSpeed; // Moving away from the destination
        }

        double eta = distance / Math.abs(currentSpeed);

        long hours = (long) eta / 3600;
        long minutes = ((long) eta % 3600) / 60;

        if (hours > 0) {
            return String.format("ETA: §a%d§r hours, §a%d§r minutes", hours, minutes);
        } else {
            return String.format("ETA: §a%d§r minutes", minutes);
        }
    }

    private void updateSpeed() {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            assert mc.player != null;
            double currentX = mc.player.getX();
            double currentZ = mc.player.getZ();

            double distance = Math.sqrt(Math.pow(currentX - lastX, 2) + Math.pow(currentZ - lastZ, 2));
            double timeElapsed = (currentTime - lastUpdateTime) / 1000.0;

            double newSpeed = distance / timeElapsed;

            int speedMeasurementsLimit = 20;
            if (speedMeasurements.size() >= speedMeasurementsLimit) {
                speedMeasurements.poll();
            }
            speedMeasurements.add(newSpeed);

            currentSpeed = speedMeasurements.stream().mapToDouble(Double::doubleValue).average().orElse(newSpeed);

            lastUpdateTime = currentTime;
            lastX = currentX;
            lastZ = currentZ;
        } finally {
            lock.unlock();
        }
    }

}
