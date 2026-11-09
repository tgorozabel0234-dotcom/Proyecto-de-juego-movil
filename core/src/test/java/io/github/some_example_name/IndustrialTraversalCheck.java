package io.github.some_example_name;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public final class IndustrialTraversalCheck {
    public static void main(String[] args) {
        for(boolean pipe : new boolean[]{false,true}) {
            for(int side : new int[]{-1,1}) {
                PlayerBody body = new PlayerBody();
                body.addSolid(2,4,3,1,pipe);
                body.reset(side < 0 ? 2-PlayerBody.WIDTH : 5,4.1f);
                body.update(1f/60,0,true,false,false,true);
                if ((body.getVelocityY()>0)!=pipe || (pipe && body.getVelocityX()*side<=0))
                    throw new AssertionError("Rebote lateral incorrecto");
            }
        }
        JsonValue vents=new JsonReader().parse("[{x:2,y:4,width:1,height:3,phase:0,period:6,blast:1,warning:1}]");
        PlayerBody body=new PlayerBody();
        body.reset(2,4);
        PlayerVitals life=new PlayerVitals();
        life.reset();
        SteamVent.contact(vents,5.5f,body,life);
        if(life.getHealth()!=10)throw new AssertionError("El aviso causa daño");
        body.update(1f/60,1,false,true,false,true);
        SteamVent.contact(vents,.2f,body,life);
        if(life.getHealth()!=10)throw new AssertionError("El dash debe cruzar el vapor");
        body.reset(2,4);
        SteamVent.contact(vents,.2f,body,life);
        SteamVent.contact(vents,.21f,body,life);
        if(life.getHealth()!=9)throw new AssertionError("Contacto o protección incorrectos");
        System.out.println("PASS: tuberías por ambos lados, aviso, vapor y dash");
    }
}
