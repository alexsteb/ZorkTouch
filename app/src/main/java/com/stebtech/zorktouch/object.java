package com.stebtech.zorktouch;

import android.util.Log;

/**
 * Created by alexanderstebner on 25.02.18.
 */

public class object {
    static int MAX_OBJECT =2000;

    static int O1_PARENT =4;
    static int O1_SIBLING =5;
    static int O1_CHILD =6;
    static int O1_PROPERTY_OFFSET =7;
    static int O1_SIZE =9;

    static int O4_PARENT =6;
    static int O4_SIBLING =8;
    static int O4_CHILD =10;
    static int O4_PROPERTY_OFFSET =12;
    static int O4_SIZE =14;



    /*
    * object_address
    *
    * Calculate the address of an object.
    *
    */
    static int object_address (int obj)
    {
/*    zchar obj_num[10]; */

    /* Check object number */

        if (obj > ((header.h_version <= 3) ? 255 : MAX_OBJECT)) {
            Log.i("alex","@Attempt to address illegal object");

            err.runtime_error (header.ERR_ILL_OBJ);
        }

    /* Return object address */

        if (header.h_version <= 3)
            return header.h_objects + ((obj - 1) * O1_SIZE + 62);
        else
            return header.h_objects + ((obj - 1) * O4_SIZE + 126);
    }


    public static int get_child(int obj){
        int obj_addr;


        obj_addr = object_address (obj);

        if (header.h_version <= 3) {

            int child;

	/* Get child id from object */

            obj_addr += O1_CHILD;
            child = header.LOW_BYTE (obj_addr);

	/* process.store child id and branch */

            return child;

        } else {

            int child;

	/* Get child id from object */

            obj_addr += O4_CHILD;
            child = header.LOW_WORD (obj_addr);

	/* process.store child id and branch */

           return child;

        }

    }


    /*
 * z_get_child, process.store the child of an object.
 *
 *	zargs[0] = object
 *
 */
    public static void z_get_child ()
    {
        int obj_addr;


        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_GET_CHILD_0);
            process.store (0);
            process.branch (false);
            return;
        }

        obj_addr = object_address (header.zargs[0]);

        if (header.h_version <= 3) {

            int child;

	/* Get child id from object */

            obj_addr += O1_CHILD;
            child = header.LOW_BYTE (obj_addr);

	/* process.store child id and branch */

            process.store (child);
            process.branch (child != 0);

        } else {

            int child;

	/* Get child id from object */

            obj_addr += O4_CHILD;
            child = header.LOW_WORD (obj_addr);

	/* process.store child id and branch */

            process.store (child);
            process.branch (child != 0);

        }

    }

    /*
     * object_name
     *
     * Return the address of the given object's name.
     *
     */
    public static int object_name (int object)
    {
        int obj_addr;
        int name_addr;

        obj_addr = object_address (object);

    /* The object name address is found at the start of the properties */

        if (header.h_version <= 3)
            obj_addr += O1_PROPERTY_OFFSET;
        else
            obj_addr += O4_PROPERTY_OFFSET;

        name_addr = header.LOW_WORD (obj_addr);

        return name_addr;

    }

    /*
 * first_property
 *
 * Calculate the start address of the property list associated with
 * an object.
 *
 */
    public static int first_property (int obj)
    {
        int prop_addr;
        int size;

    /* Fetch address of object name */

        prop_addr = object_name (obj);

    /* Get length of object name */

        size = header.LOW_BYTE (prop_addr);

    /* Add name length to pointer */

        return prop_addr + 1 + 2 * size;

    }/* first_property */


    /*
     * next_property
     *
     * Calculate the address of the next property in a property list.
     *
     */
    public static int next_property (int prop_addr)
    {
        int value;

    /* Load the current property id */

        value = header.LOW_BYTE (prop_addr);
        prop_addr++;

    /* Calculate the length of this property */

        if (header.h_version <= 3)
            value >>= 5;
        else if ((value & 0x80) == 0)
            value >>= 6;
        else {

            value = header.LOW_BYTE (prop_addr);
            value &= 0x3f;

            if (value == 0) value = 64;	/* demanded by Spec 1.0 */

        }

    /* Add property length to current property pointer */

        return prop_addr + value + 1;

    }



    /*
 * unlink_object
 *
 * Unlink an object from its parent and siblings.
 *
 */
    public static void unlink_object (int object)
    {
        int obj_addr;
        int parent_addr;
        int sibling_addr;

        if (object == 0) {
            err.runtime_error (header.ERR_REMOVE_OBJECT_0);
            return;
        }

        obj_addr = object_address (object);

        if (header.h_version <= 3) {

            int parent;
            int younger_sibling;
            int older_sibling;
            int zero = 0;

	/* Get parent of object, and return if no parent */

            obj_addr += O1_PARENT;
            parent = header.LOW_BYTE (obj_addr);
            if (parent == 0)
                return;

	/* Get (older) sibling of object and set both parent and sibling
	   pointers to 0 */

            header.SET_BYTE (obj_addr, zero);
            obj_addr += O1_SIBLING - O1_PARENT;
            older_sibling = header.LOW_BYTE (obj_addr);
            header.SET_BYTE (obj_addr, zero);

	/* Get first child of parent (the youngest sibling of the object) */

            parent_addr = object_address (parent) + O1_CHILD;
            younger_sibling = header.LOW_BYTE (parent_addr);

	/* Remove object from the list of siblings */

            if (younger_sibling == object)
                header.SET_BYTE (parent_addr, older_sibling);
            else {
                do {
                    sibling_addr = object_address (younger_sibling) + O1_SIBLING;
                    younger_sibling = header.LOW_BYTE (sibling_addr);
                } while (younger_sibling != object);
                header.SET_BYTE (sibling_addr, older_sibling);
            }

        } else {

            int parent;
            int younger_sibling;
            int older_sibling;
            int zero = 0;

	/* Get parent of object, and return if no parent */

            obj_addr += O4_PARENT;
            parent = header.LOW_WORD (obj_addr);
            if (parent == 0)
                return;

	/* Get (older) sibling of object and set both parent and sibling
	   pointers to 0 */

            header.SET_WORD (obj_addr, zero);
            obj_addr += O4_SIBLING - O4_PARENT;
            older_sibling = header.LOW_WORD (obj_addr);
            header.SET_WORD (obj_addr, zero);

	/* Get first child of parent (the youngest sibling of the object) */

            parent_addr = object_address (parent) + O4_CHILD;
            younger_sibling = header.LOW_WORD (parent_addr);

	/* Remove object from the list of siblings */

            if (younger_sibling == object)
                header.SET_WORD (parent_addr, older_sibling);
            else {
                do {
                    sibling_addr = object_address (younger_sibling) + O4_SIBLING;
                    younger_sibling = header.LOW_WORD (sibling_addr);
                } while (younger_sibling != object);
                header.SET_WORD (sibling_addr, older_sibling);
            }

        }

    }


    /*
 * z_clear_attr, clear an object attribute.
 *
 *	zargs[0] = object
 *	zargs[1] = number of attribute to be cleared
 *
 */
    public static void z_clear_attr ()
    {
        int obj_addr;
        int value;

        if (header.story_id == story.SHERLOCK)
            if (header.zargs[1] == 48)
                return;

        if (header.zargs[1] > ((header.h_version <= 3) ? 31 : 47))
            err.runtime_error (header.ERR_ILL_ATTR);

    /* If we are monitoring attribute assignment display a short note */

        /*if (f_setup.attribute_assignment) {
            stream_mssg_on ();
            print_string ("@clear_attr ");
            print_object (zargs[0]);
            print_string (" ");
            print_num (zargs[1]);
            stream_mssg_off ();
        }*/

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_CLEAR_ATTR_0);
            return;
        }

    /* Get attribute address */

        obj_addr = object_address (header.zargs[0]) + header.zargs[1] / 8;

    /* Clear attribute bit */

        value = header.LOW_BYTE (obj_addr);
        value &= ~(0x80 >> (header.zargs[1] & 7));
        header.SET_BYTE (obj_addr, value);

    }


    /*
 * z_jin, branch if the first object is inside the second.
 *
 *	zargs[0] = first object
 *	zargs[1] = second object
 *
 */
    public static void z_jin ()
    {
        int obj_addr;

    /* If we are monitoring object locating display a short note */

        /*if (f_setup.object_locating) {
            stream_mssg_on ();
            print_string ("@jin ");
            print_object (zargs[0]);
            print_string (" ");
            print_object (zargs[1]);
            stream_mssg_off ();
        }*/

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_JIN_0);
            process.branch (0 == header.zargs[1]);
            return;
        }

        obj_addr = object_address (header.zargs[0]);

        if (header.h_version <= 3) {

            int parent;

	/* Get parent id from object */

            obj_addr += O1_PARENT;
            parent = header.LOW_BYTE (obj_addr);

	/* Branch if the parent is obj2 */

            process.branch (parent == header.zargs[1]);

        } else {

            int parent;

	/* Get parent id from object */

            obj_addr += O4_PARENT;
            parent = header.LOW_WORD (obj_addr);

	/* Branch if the parent is obj2 */

            process.branch (parent == header.zargs[1]);

        }

    }


    /*
 * z_get_next_prop, process.store the number of the first or next property.
 *
 *	zargs[0] = object
 *	zargs[1] = address of current property (0 gets the first property)
 *
 */
    public static void z_get_next_prop ()
    {
        int prop_addr;
        int value;
        int mask;

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_GET_NEXT_PROP_0);
            process.store (0);
            return;
        }

    /* Property id is in bottom five (six) bits */

        mask = (header.h_version <= 3) ? 0x1f : 0x3f;

    /* Load address of first property */

        prop_addr = first_property (header.zargs[0]);

        if (header.zargs[1] != 0) {

	/* Scan down the property list */

            do {
                value = header.LOW_BYTE (prop_addr);
                prop_addr = next_property (prop_addr);
            } while ((value & mask) > header.zargs[1]);

	/* Exit if the property does not exist */

            if ((value & mask) != header.zargs[1])
                err.runtime_error (header.ERR_NO_PROP);

        }

    /* Return the property id */

        value = header.LOW_BYTE (prop_addr);
        process.store ((int) (value & mask));

    }



    public static int get_parent(int obj){
        int obj_addr = object_address (obj);

        if (header.h_version <= 3) {

            int parent;

	/* Get parent id from object */

            obj_addr += O1_PARENT;
            parent = header.LOW_BYTE (obj_addr);
            return parent;

        } else {

            int parent;

	/* Get parent id from object */

            obj_addr += O4_PARENT;
            parent = header.LOW_WORD (obj_addr);
            return parent;
        }

    }

    /*
 * z_get_parent, process.store the parent of an object.
 *
 *	zargs[0] = object
 *
 */
    public static void z_get_parent ()
    {
        int obj_addr;

    /* If we are monitoring object locating display a short note */

        /*if (f_setup.object_locating) {
            stream_mssg_on ();
            print_string ("@get_parent ");
            print_object (zargs[0]);
            stream_mssg_off ();
        }*/

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_GET_PARENT_0);
            process.store (0);
            return;
        }

        obj_addr = object_address (header.zargs[0]);

        if (header.h_version <= 3) {

            int parent;

	/* Get parent id from object */

            obj_addr += O1_PARENT;
            parent = header.LOW_BYTE (obj_addr);

	/* process.store parent */

            process.store (parent);

        } else {

            int parent;

	/* Get parent id from object */

            obj_addr += O4_PARENT;
            parent = header.LOW_WORD (obj_addr);

	/* process.store parent */

            process.store (parent);

        }

    }/* z_get_parent */


    /*
     * z_get_prop, process.store the value of an object property.
     *
     *	header.zargs[0] = object
     *	header.zargs[1] = number of property to be examined
     *
     */
    public static void z_get_prop ()
    {
        int prop_addr;
        int wprop_val;
        int bprop_val;
        int value;
        int mask;

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_GET_PROP_0);
            process.store (0);
            return;
        }

    /* Property id is in bottom five (six) bits */

        mask = (header.h_version <= 3) ? 0x1f : 0x3f;

    /* Load address of first property */

        prop_addr = first_property (header.zargs[0]);

    /* Scan down the property list */

        for (;;) {
            value = header.LOW_BYTE (prop_addr);
            if ((value & mask) <= header.zargs[1])
                break;
            prop_addr = next_property (prop_addr);
        }

        if ((value & mask) == header.zargs[1]) {	/* property found */

	/* Load property (byte or word sized) */

            prop_addr++;

            if ((header.h_version <= 3 && ((value & 0xe0) == 0)) || (header.h_version >= 4 && ((value & 0xc0)==0))) {

                bprop_val = header.LOW_BYTE (prop_addr);
                wprop_val = bprop_val;

            } else wprop_val = header.LOW_WORD (prop_addr);

        } else {	/* property not found */

	/* Load default value */

            prop_addr = header.h_objects + 2 * (header.zargs[1] - 1);
            wprop_val = header.LOW_WORD (prop_addr);

        }

    /* process.store the property value */

        process.store (wprop_val);

    }/* z_get_prop */


    /*
     * z_get_prop_addr, process.store the address of an object property.
     *
     *	header.zargs[0] = object
     *	header.zargs[1] = number of property to be examined
     *
     */
    public static void z_get_prop_addr ()
    {
        int prop_addr;
        int value;
        int mask;

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_GET_PROP_ADDR_0);
            process.store (0);
            return;
        }

        if (header.story_id == story.BEYOND_ZORK)
            if (header.zargs[0] > MAX_OBJECT)
            { process.store (0); return; }

    /* Property id is in bottom five (six) bits */

        mask = (header.h_version <= 3) ? 0x1f : 0x3f;

    /* Load address of first property */

        prop_addr = first_property (header.zargs[0]);

    /* Scan down the property list */

        for (;;) {
            value = header.LOW_BYTE (prop_addr);
            if ((value & mask) <= header.zargs[1])
                break;
            prop_addr = next_property (prop_addr);
        }

    /* Calculate the property address or return zero */

        if ((value & mask) == header.zargs[1]) {

            if ((header.h_version >= 4) && ((value & 0x80) != 0))
                prop_addr++;
            process.store ((int) (prop_addr + 1));

        } else process.store (0);

    }/* z_get_prop_addr */


    /*
     * z_get_prop_len, process.store the length of an object property.
     *
     * 	header.zargs[0] = address of property to be examined
     *
     */
    public static void z_get_prop_len ()
    {
        int addr;
        int value;

    /* Back up the property pointer to the property id */

        addr = header.zargs[0] - 1;
        value = header.LOW_BYTE (addr);

    /* Calculate length of property */

        if (header.h_version <= 3)
            value = (value >> 5) + 1;
        else if ((value & 0x80) == 0)
            value = (value >> 6) + 1;
        else {

            value &= 0x3f;

            if (value == 0) value = 64;	/* demanded by Spec 1.0 */

        }

    /* process.store length of property */

        process.store (value);

    }/* z_get_prop_len */




    public static int get_sibling(int obj){
       int obj_addr = object_address (obj);

        if (header.h_version <= 3) {

            int sibling;

	/* Get sibling id from object */

            obj_addr += O1_SIBLING;
            sibling = header.LOW_BYTE (obj_addr);

	/* process.store sibling and branch */

          return sibling;
        } else {

            int sibling;

	/* Get sibling id from object */

            obj_addr += O4_SIBLING;
            sibling = header.LOW_WORD (obj_addr);

	/* process.store sibling and branch */

        return sibling;
        }
    }

    /*
     * z_get_sibling, process.store the sibling of an object.
     *
     *	header.zargs[0] = object
     *
     */
    public static void z_get_sibling ()
    {
        int obj_addr;

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_GET_SIBLING_0);
            process.store (0);
            process.branch (false);
            return;
        }

        obj_addr = object_address (header.zargs[0]);

        if (header.h_version <= 3) {

            int sibling;

	/* Get sibling id from object */

            obj_addr += O1_SIBLING;
            sibling = header.LOW_BYTE (obj_addr);

	/* process.store sibling and branch */

            process.store (sibling);
            process.branch (sibling != 0);

        } else {

            int sibling;

	/* Get sibling id from object */

            obj_addr += O4_SIBLING;
            sibling = header.LOW_WORD (obj_addr);

	/* process.store sibling and branch */

            process.store (sibling);
            process.branch (sibling != 0);

        }

    }/* z_get_sibling */


    /*
     * z_insert_obj, make an object the first child of another object.
     *
     *	header.zargs[0] = object to be moved
     *	header.zargs[1] = destination object
     *
     */
    public static void z_insert_obj ()
    {
        int obj1 = header.zargs[0];
        int obj2 = header.zargs[1];
        int obj1_addr;
        int obj2_addr;

    /* If we are monitoring object movements display a short note */

        /*if (f_setup.object_movement) {
            stream_mssg_on ();
            print_string ("@move_obj ");
            print_object (obj1);
            print_string (" ");
            print_object (obj2);
            stream_mssg_off ();
        }*/

        if (obj1 == 0) {
            err.runtime_error (header.ERR_MOVE_OBJECT_0);
            return;
        }

        if (obj2 == 0) {
            err.runtime_error (header.ERR_MOVE_OBJECT_TO_0);
            return;
        }

    /* Get addresses of both objects */

        obj1_addr = object_address (obj1);
        obj2_addr = object_address (obj2);

    /* Remove object 1 from current parent */

        unlink_object (obj1);

    /* Make object 1 first child of object 2 */

        if (header.h_version <= 3) {

            int child;

            obj1_addr += O1_PARENT;
            header.SET_BYTE (obj1_addr, obj2);
            obj2_addr += O1_CHILD;
            child = header.LOW_BYTE (obj2_addr);
            header.SET_BYTE (obj2_addr, obj1);
            obj1_addr += O1_SIBLING - O1_PARENT;
            header.SET_BYTE (obj1_addr, child);

        } else {

            int child;

            obj1_addr += O4_PARENT;
            header.SET_WORD (obj1_addr, obj2);
            obj2_addr += O4_CHILD;
            child = header.LOW_WORD (obj2_addr);
            header.SET_WORD (obj2_addr, obj1);
            obj1_addr += O4_SIBLING - O4_PARENT;
            header.SET_WORD (obj1_addr, child);

        }

    }



    /*
 * z_put_prop, set the value of an object property.
 *
 *	header.zargs[0] = object
 *	header.zargs[1] = number of property to set
 *	header.zargs[2] = value to set property to
 *
 */
    public static void z_put_prop ()
    {
        int prop_addr;
        int value;
        int mask;

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_PUT_PROP_0);
            return;
        }

    /* Property id is in bottom five or six bits */

        mask = (header.h_version <= 3) ? 0x1f : 0x3f;

    /* Load address of first property */

        prop_addr = first_property (header.zargs[0]);

    /* Scan down the property list */

        for (;;) {
            value = header.LOW_BYTE (prop_addr);
            if ((value & mask) <= header.zargs[1])
                break;
            prop_addr = next_property (prop_addr);
        }

    /* Exit if the property does not exist */

        if ((value & mask) != header.zargs[1])
            err.runtime_error (header.ERR_NO_PROP);

    /* process.store the new property value (byte or word sized) */

        prop_addr++;

        if ((header.h_version <= 3 && (value & 0xe0)==0) || (header.h_version >= 4 && (value & 0xc0)==0)) {
            int v = header.zargs[2];
            header.SET_BYTE (prop_addr, v);
        } else {
            int v = header.zargs[2];
            header.SET_WORD (prop_addr, v);
        }

    }

    /*
 * z_remove_obj, unlink an object from its parent and siblings.
 *
 *	zargs[0] = object
 *
 */
    public static void z_remove_obj ()
    {
    /* If we are monitoring object movements display a short note */

       /* if (f_setup.object_movement) {
            stream_mssg_on ();
            print_string ("@remove_obj ");
            print_object (zargs[0]);
            stream_mssg_off ();
        }*/

    /* Call unlink_object to do the job */

        unlink_object (header.zargs[0]);

    }




    /*
 * z_set_attr, set an object attribute.
 *
 *	zargs[0] = object
 *	zargs[1] = number of attribute to set
 *
 */
    public static void z_set_attr ()
    {
        int obj_addr;
        int value;

        if (header.story_id == story.SHERLOCK)
            if (header.zargs[1] == 48)
                return;

        if (header.zargs[1] > ((header.h_version <= 3) ? 31 : 47))
            err.runtime_error (header.ERR_ILL_ATTR);

    /* If we are monitoring attribute assignment display a short note */

       /* if (f_setup.attribute_assignment) {
            stream_mssg_on ();
            print_string ("@set_attr ");
            print_object (zargs[0]);
            print_string (" ");
            print_num (zargs[1]);
            stream_mssg_off ();
        }*/

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_SET_ATTR_0);
            return;
        }

    /* Get attribute address */

        obj_addr = object_address (header.zargs[0]) + header.zargs[1] / 8;

    /* Load attribute byte */

        value = header.LOW_BYTE (obj_addr);

    /* Set attribute bit */

        value |= 0x80 >> (header.zargs[1] & 7);

    /* process.store attribute byte */

        header.SET_BYTE (obj_addr, value);

    }/* z_set_attr */


    /*
     * z_test_attr, branch if an object attribute is set.
     *
     *	zargs[0] = object
     *	zargs[1] = number of attribute to test
     *
     */
    public static void z_test_attr ()
    {
        int obj_addr;
        int value;

        if (header.zargs[1] > ((header.h_version <= 3) ? 31 : 47))
            err.runtime_error (header.ERR_ILL_ATTR);

    /* If we are monitoring attribute testing display a short note */

        /*if (f_setup.attribute_testing) {
            stream_mssg_on ();
            print_string ("@test_attr ");
            print_object (zargs[0]);
            print_string (" ");
            print_num (zargs[1]);
            stream_mssg_off ();
        }*/

        if (header.zargs[0] == 0) {
            err.runtime_error (header.ERR_TEST_ATTR_0);
            process.branch (false);
            return;
        }

    /* Get attribute address */

        obj_addr = object_address (header.zargs[0]) + header.zargs[1] / 8;

    /* Load attribute byte */

        value = header.LOW_BYTE (obj_addr);

    /* Test attribute */

        process.branch ((value & (0x80 >> (header.zargs[1] & 7))) != 0);

    }
}
