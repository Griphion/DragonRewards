package me.griphion.DragonRewards.Utils;

import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class HomingDragonFireballTask extends BukkitRunnable {

    private static final double MaxRotationAngle = 0.15D;//Base de 0.12
    //private static final double TargetSpeed = 1.4D;
    DragonFireball dragonFireball;
    Player target;

    public HomingDragonFireballTask(DragonFireball dragonFireball, Player target, Plugin plugin) {
        this.dragonFireball = dragonFireball;
        this.target = target;
        runTaskTimer(plugin, 1L, 1L);
    }

    public void run()
    {
        double velocidadActual = this.dragonFireball.getVelocity().length();
        if ((this.dragonFireball.isOnGround()) || (this.dragonFireball.isDead()) || (this.target.isDead()) || (this.target.getWorld() != this.dragonFireball.getWorld()) )
        {
            cancel();
            return;
        }
        Vector vectorAObjetivo = this.target.getLocation().clone().subtract(this.dragonFireball.getLocation()).toVector();
        //Vector toTarget = this.target.getLocation().clone().add(new Vector(0.0D, 0.5D, 0.0D)).subtract(this.dragonFireball.getLocation()).toVector();

        Vector direccionDelFireball = this.dragonFireball.getVelocity().clone().normalize();
        Vector direccionAlObjetivo = vectorAObjetivo.clone().normalize();
        double angulo = direccionDelFireball.angle(direccionAlObjetivo);

        if(angulo == 0) return;

        double newSpeed = 0.9D * velocidadActual + 0.14D;

        Vector nuevoVectorVelocidad; //La nueva velocidad es la que apunta al objetivo

        if (angulo < MaxRotationAngle) {
            nuevoVectorVelocidad = direccionDelFireball.clone().multiply(newSpeed);
        }
        else {
            Vector nuevaDireccion = direccionDelFireball.clone().multiply( (angulo - MaxRotationAngle) / angulo).add(direccionAlObjetivo.clone().multiply(MaxRotationAngle / angulo));
            nuevoVectorVelocidad = nuevaDireccion.clone().normalize().multiply(newSpeed);
        }
        //nuevoVectorVelocidad.add(new Vector(0.0D, 0.03D, 0.0D));

        try{
            this.dragonFireball.setVelocity(nuevoVectorVelocidad);
        }catch (IllegalArgumentException ignore){}

    }


}
